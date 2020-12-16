package com.buaa.compilec0.assembler;

public class Global {
    private String globalDataName;
    private GlobalType globalType;
    private byte[] data;

    public Global(String globalDataName, GlobalType globalType) {
        this.globalDataName = globalDataName;
        this.globalType = globalType;
        setData();
    }

    public String getGlobalDataName() {
        return globalDataName;
    }

    public void setGlobalDataName(String globalDataName) {
        this.globalDataName = globalDataName;
    }

    public GlobalType getGlobalType() {
        return globalType;
    }

    public void setGlobalType(GlobalType globalType) {
        this.globalType = globalType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setData() {
        if (this.globalType == GlobalType.VARIABLE || this.globalType == GlobalType.CONSTANT) {
            data = new byte[8];
            for (int i = 0; i < 8; i++) {
                data[i] = 0x00;
            }
        } else if (this.globalType == GlobalType.FUNCTION || this.globalType == GlobalType.STRING) {
            int size = globalDataName.length();
            data = globalDataName.getBytes();
        }
    }

    @Override
    public String toString() {
        StringBuilder binary = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            String hex = Integer.toHexString(data[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            binary.append(hex + " ");
        }
        return "static: " + binary + "(\'" + globalDataName + "\')";
    }

//    public static void main(String[] args) {
//        Global global = new Global("a", GlobalType.VARIABLE);
//        System.out.println(global.toString());
//    }
}
