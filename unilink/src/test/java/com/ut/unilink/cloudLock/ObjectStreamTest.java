package com.ut.unilink.cloudLock;

import com.ut.unilink.cloudLock.protocol.data.ProductInfo;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ObjectStreamTest {

    @Test
    public void testWrite() {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("test.txt");
            ObjectOutputStream out = new ObjectOutputStream(fileOutputStream);
            CloudLock cloudLock = new CloudLock("123456");
            ProductInfo productInfo = new ProductInfo();
            productInfo.setSerialNum("1111".getBytes());
            cloudLock.setProductInfo(productInfo);
            cloudLock.setAdminPassword("888888".getBytes());
            out.writeObject(cloudLock);
            System.out.println("write success");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testRead() {
        try {
            FileInputStream fileInputStream = new FileInputStream("test.txt");
            ObjectInputStream in = new ObjectInputStream(fileInputStream);
            CloudLock cloudLock = (CloudLock) in.readObject();
            System.out.println("object:" + cloudLock.getAddress() + " " + new String(cloudLock.getAdminPassword()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    static class MyObject implements Serializable {
        int a;
        int b;
    }
}
