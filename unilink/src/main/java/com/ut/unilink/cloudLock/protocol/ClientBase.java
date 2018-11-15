package com.ut.unilink.cloudLock.protocol;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class ClientBase {

	public ExecutorService receiveExecutor = Executors.newSingleThreadExecutor();                        //负责不停接收消息
	public ScheduledExecutorService handleExecutor = Executors.newSingleThreadScheduledExecutor();          //负责处理接受的消息、发送消息

	public abstract void send(byte[] msg);
	public abstract void receive(byte[] data);
	public abstract void broadcastSend(byte[] msg);
	public abstract void open();
	public boolean isConnect() {
		return false;
	}

	public void close() {
		receiveExecutor.shutdown();
		handleExecutor.shutdown();
	}

	public boolean isUseBroadcast() {            //是否是广播的通讯方式
		return false;
	}

	public ReceiveListener receiveListener;

	public void setReceiveListener(ReceiveListener receiveListener) {
		this.receiveListener = receiveListener;
	}

	public interface ReceiveListener {
		void onReceive(byte[] msg);
	}
}
