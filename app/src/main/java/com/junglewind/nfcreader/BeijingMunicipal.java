package com.junglewind.nfcreader;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by swp on 7/6/16.
 */
public class BeijingMunicipal {

    private final IsoDep nfcTag;

    private int balance;
    private List<UseLog> useLogs;

    public BeijingMunicipal(Tag rawTag) {
        this.nfcTag = IsoDep.get(rawTag);
    }

    public boolean verifyAndRead() {
        boolean allCool = false;
        if (nfcTag == null) {
            Log.e("BeijingMunicipal", "nfcTag is null");
            return false;
        }
        Log.i("BeijingMunicipal", "can send max bytes " + nfcTag.getMaxTransceiveLength());
        nfcTag.setTimeout(3000);
        try {
            nfcTag.connect();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("BeijingMunicipal", "can not connect to nfcTag");
            return false;
        }
        if (selectPayFile()) {
            if (selectApplication()) {
                if (readBalance()) {
                    allCool = true;
                }
            }
        }

        if (nfcTag.isConnected()) {
            try {
                nfcTag.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("BeijingMunicipal", "can not close nfcTag");
            }
        }
        return allCool;
    }

    private boolean readBalance() {
        final byte[] cmd = {(byte) 0x80, // CLA Class
                (byte) 0x5C, // INS Instruction
                (byte) 0x00, // P1 Parameter 1
                (byte) 0x02, // P2 Parameter 2
                (byte) 0x04, // Le
        };

        try {
            byte[] response = nfcTag.transceive(cmd);
            if (isOk(response)) {
                if (response.length > 4) {
                    balance = (int) ((response[0] << 24 & 0xFF000000) | (response[1] << 16 & 0x00FF0000) | (response[2] << 8 & 0x0000FF00) | (response[3] & 0xFF));
                    Log.e("BeijingMunicipal", "balance = " + balance);
                    return true;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean readUseLogs() {
        return false;
    }

    private boolean selectPayFile() {
        byte[] DFN_PSE = {(byte) '1', (byte) 'P',
                (byte) 'A', (byte) 'Y', (byte) '.', (byte) 'S', (byte) 'Y',
                (byte) 'S', (byte) '.', (byte) 'D', (byte) 'D', (byte) 'F',
                (byte) '0', (byte) '1',};
        ByteBuffer cmd = ByteBuffer.allocate(DFN_PSE.length + 6);
        cmd.put((byte) 0x00) // CLA
                .put((byte) 0xA4) // INS
                .put((byte) 0x04) // P1
                .put((byte) 0x00) // P2
                .put((byte) DFN_PSE.length) // Lc
                .put(DFN_PSE) // Nc
                .put((byte) 0x00); // Le

        try {
            byte[] response = nfcTag.transceive(cmd.array());
            if (isOk(response)) {
                Log.i("BeijingMunicipal", "select pay file ok");
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean selectApplication() {
        byte[] DFI_EP = {(byte) 0x10, (byte) 0x01};
        ByteBuffer cmd = ByteBuffer.allocate(DFI_EP.length + 6);
        cmd.put((byte) 0x00) // CLA
                .put((byte) 0xA4) // INS
                .put((byte) 0x00) // P1
                .put((byte) 0x00) // P2
                .put((byte) DFI_EP.length) // Lc
                .put(DFI_EP) // Nc
                .put((byte) 0x00); // Le
        try {
            byte[] response = nfcTag.transceive(cmd.array());
            if (isOk(response)) {
                Log.i("BeijingMunicipal", "select application ok");
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean isOk(byte[] data) {
        if (data.length < 2) {
            return false;
        }

        Log.d("BeijingMunicipal", "response hex: " + bytesToHex(data));

        byte last1 = data[data.length - 1];
        byte last2 = data[data.length - 2];

        short expected = (short) 0x9000;
        short result = (short) ((last2 << 8 & 0xFF00) | (last1 & 0xFF));
        return (expected == result);
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public int getBalance() {
        return balance;
    }

    public List<UseLog> getUseLogs() {
        return useLogs;
    }

    public class UseLog {
        private long timestamp;
        private int money;
        private String machineId;

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public int getMoney() {
            return money;
        }

        public void setMoney(int money) {
            this.money = money;
        }

        public String getMachineId() {
            return machineId;
        }

        public void setMachineId(String machineId) {
            this.machineId = machineId;
        }
    }
}
