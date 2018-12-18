package com.ut.unilink.cloudLock.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientHelper implements ClientBase.ReceiveListener {
	private ClientBase client;
	private Map<Integer, SendedTask> sendedTaskMap = new HashMap<>();
	private ScheduledExecutorService executorService;
	private IEncrypt mEncrypt;
	private static AtomicInteger value = new AtomicInteger();

	private static final int TIMEOUT = 1000;

	public ClientHelper(ClientBase client) {
		this.client = client;
		client.setReceiveListener(this);
		executorService = client.handleExecutor;
	}

	public ClientBase getClient() {
		return client;
	}

	public void setClient(ClientBase client) {
		this.client = client;
	}

	public void asyncSend(final BleMsg msg, final ResponseListener listener) {

		if (executorService.isShutdown()) {
			return;
		}

		executorService.schedule(new Runnable() {
			@Override
			public void run() {
				send(msg, listener);
			}
		}, 0, TimeUnit.MILLISECONDS);
	}

	public void asyncSend(final BleMsg msg) {

		if (executorService.isShutdown()) {
			return;
		}

		executorService.schedule(new Runnable() {
			@Override
			public void run() {
				send(msg);
			}
		}, 0, TimeUnit.MILLISECONDS);
	}

	public void send(BleMsg msg, ResponseListener listener) {
		if (client == null) {
			return;
		}

		final int requestID = value.get() % 256;
		value.incrementAndGet();
		msg.setRequestID(requestID);

		ScheduledFuture scheduledFuture = executorService.schedule(new Runnable() {
				@Override
				public void run() {
					SendedTask task = sendedTaskMap.remove(requestID);

					if (task != null) {
						ResponseListener listener = task.responseListener;
						BleMsg sendMsg = task.sendmsg;

						if (listener != null) {
							listener.timeout(sendMsg);
						}
					}
				}
		}, TIMEOUT, TimeUnit.MILLISECONDS);

		SendedTask sendedTask = new SendedTask(msg, scheduledFuture);
		sendedTask.setResponseListener(listener);
		sendedTaskMap.put(requestID, sendedTask);

		send(msg);
	}

	/**
	 * 将消息对象转换成字节流发送，并在字节流末尾附加3个字节，附加字节中第一个字节放加密方式标识（动态加密0 固定加密1）
	 * 第2个字节放是否加密标识，第3个字节放入requestID,用来当做帧处理层的命令ID
	 * @param msg
	 */
	private void send(BleMsg msg) {

		IEncrypt encrypt = getEncrypt(msg.getEncryptType());
		msg.setEntrypt(encrypt);
		byte[] data = msg.encode();

		byte[] wrapData = new byte[data.length + 3];
		System.arraycopy(data, 0, wrapData, 0, data.length);
		wrapData[wrapData.length - 1] = (byte) msg.getRequestID();    			//最后一个字节放入requestID,用来当做帧处理层的命令ID
		wrapData[wrapData.length - 2] = (byte) (msg.isEncrypt() ? 1 : 0);       //倒数第二个字节放是否加密标识
		wrapData[wrapData.length - 3] = (byte) (msg.getEncryptType() == BleMsg.ENCRYPT_TYPE_FIXED ? 1 : 0);

		client.send(wrapData);
	}

	private IEncrypt getEncrypt(int encryptType) {

		IEncrypt encrypt = null;

		switch (encryptType) {
			case BleMsg.ENCRYPT_TYPE_FIXED:
				byte[] key = new byte[]{0x14, 0x18, (byte) 0x82, 0x02, (byte) 0xE9, 0x6B, (byte) 0x88, (byte) 0xAD,
						(byte) 0xFF, 0x0C, 0x11, 0x79, (byte) 0xAF, 0x39, 0x5B, (byte) 0xEE};
				encrypt =  new TeaEncrypt(key);       //这几条采用固定加密的方式
				break;

			case BleMsg.ENCRYPT_TYPE_DYNAMIC:
				encrypt =  mEncrypt;
				break;

			case BleMsg.ENCRYPT_TYPE_NO:
				encrypt = BleMsg.NO_ENCRYPT;
				break;

			default:
		}
		return encrypt;
    }

	public boolean isConnect() {
		return client.isConnect();
	}
	
	public void close() {
		if (client != null) {
			client.close();
			client = null;
		}
	}

	public void setEncrypt(IEncrypt mEncrypt) {
		this.mEncrypt = mEncrypt;
	}

	/**
	 *处理收到的报文内容
	 * @param msg 报文内容（除去帧结构部分），最后2个字节为附加字节，倒数第一个字节为cmdID， 倒数第二个字节为是否加密（0或1）
	 *            倒数第三个字节为加密方式标识（动态加密0 固定加密1）
	 */
	@Override
	public void onReceive(byte[] msg) {

		int requestID = msg[msg.length - 1];
		byte isEncrypt = msg[msg.length - 2];
		byte encryptType = msg[msg.length - 3];

		byte[] data = new byte[msg.length - 3];
		System.arraycopy(msg, 0, data, 0, data.length);

		IEncrypt encrypt = getEncrypt(encryptType);
		BleMsg responseMsg = BleMsg.decode(data, encrypt);

		if (responseMsg.getCode() != 0x25) {     //功能码0x25是从设备主动上传的报文

			SendedTask task = sendedTaskMap.remove(requestID);
			if (task != null) {

				boolean isResponseError = responseMsg.isResponseError();

				task.scheduledFuture.cancel(true);
				task.scheduledFuture = null;

				if (task.responseListener != null) {
					BleMsg sendedMsg = task.sendmsg;

					if (!isResponseError) {
						task.responseListener.onACk(sendedMsg, responseMsg);
					} else {
						int errCode = responseMsg.getContent()[0];
						task.responseListener.onNAk(sendedMsg, errCode);
					}
				}
			}
		}
		
		if (receiveListener != null) {
			receiveListener.onReceive(responseMsg);
		}
	}

	private ReceiveListener receiveListener;

	public void setReceiveListener(ReceiveListener receiveListener) {
		this.receiveListener = receiveListener;
	}

	public interface ReceiveListener {
		void onReceive(BleMsg msg);
	}

	public interface ResponseListener {
		void onACk(BleMsg sendMsg, BleMsg responseMsg);
		void onNAk(BleMsg sendMsg, int errCode);
		void timeout(BleMsg sendMsg);
	}
	
	public static class SendedTask {
		BleMsg sendmsg;
		ScheduledFuture scheduledFuture;
		private ResponseListener responseListener;
		
		public SendedTask(BleMsg sendmsg, ScheduledFuture scheduledFuture) {
			this.sendmsg = sendmsg;
			this.scheduledFuture = scheduledFuture;
		}

		public void setResponseListener(ResponseListener responseListener) {
			this.responseListener = responseListener;
		}
	}
}
