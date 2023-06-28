package ObjectPackage;

import java.io.Serializable;

public class Chunk implements Serializable {
    private int chunkNo;
    private int length;
    private byte[] data;

    public Chunk(int chunkNo, int length, byte[] data) {
        this.chunkNo = chunkNo;
        this.length = length;
        this.data = data;
    }

    public int getChunkNo() {
        return chunkNo;
    }

    public void setChunkNo(int chunkNo) {
        this.chunkNo = chunkNo;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
