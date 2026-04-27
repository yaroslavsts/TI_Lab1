package rsa;

public class RSA {
    private long p;
    private long q;
    private long n;
    private long phi;
    private long kc;
    private long ko;

    public boolean initialize(long p, long q, long kc) {
        this.p = p;
        this.q = q;
        this.n = p * q;
        this.phi = (p - 1) * (q - 1);
        this.kc = kc;

        try {
            this.ko = MathTools.modInverse(this.kc, this.phi);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public byte[] encryptData(byte[] data) {
        byte[] result = new byte[data.length * 2];
        for (int i = 0; i < data.length; i++) {
            int m = data[i] & 0xFF;
            long c = MathTools.fastExp(m, ko, n);

            result[i * 2] = (byte) (c & 0xFF);
            result[i * 2 + 1] = (byte) ((c >> 8) & 0xFF);
        }
        return result;
    }

    public byte[] decryptData(byte[] data) {
        if (data.length % 2 != 0) {
            return null;
        }

        byte[] result = new byte[data.length / 2];
        for (int i = 0; i < data.length; i += 2) {
            int c = (data[i] & 0xFF) | ((data[i + 1] & 0xFF) << 8);
            long m = MathTools.fastExp(c, kc, n);
            result[i / 2] = (byte) (m & 0xFF);
        }
        return result;
    }

    public long getP() {
        return p;
    }

    public long getQ() {
        return q;
    }

    public long getN() {
        return n;
    }

    public long getPhi() {
        return phi;
    }

    public long getKc() {
        return kc;
    }

    public long getKo() {
        return ko;
    }
}
