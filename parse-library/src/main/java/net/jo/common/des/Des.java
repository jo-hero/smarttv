package net.jo.common.des;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Des {
    public String getBoxBinary(int i) {
        switch (i) {
            case 0:
                return "0000";
            case 1:
                return "0001";
            case 2:
                return "0010";
            case 3:
                return "0011";
            case 4:
                return "0100";
            case 5:
                return "0101";
            case 6:
                return "0110";
            case 7:
                return "0111";
            case 8:
                return "1000";
            case 9:
                return "1001";
            case 10:
                return "1010";
            case 11:
                return "1011";
            case 12:
                return "1100";
            case 13:
                return "1101";
            case 14:
                return "1110";
            case 15:
                return "1111";
            default:
                return "";
        }
    }

    public String strEnc(String str, String str2, String str3, String str4) {
        int i;
        List list;
        int i2;
        List list2;
        int i3;
        List list3;
        int[] iArr;
        int[] iArr2;
        int[] iArr3;
        int length = str.length();
        if (str2 == null || str2 == "") {
            list = null;
            i = 0;
        } else {
            list = getKeyBytes(str2);
            i = list.size();
        }
        if (str3 == null || str3 == "") {
            list2 = null;
            i2 = 0;
        } else {
            list2 = getKeyBytes(str3);
            i2 = list2.size();
        }
        if (str4 == null || str4 == "") {
            list3 = null;
            i3 = 0;
        } else {
            list3 = getKeyBytes(str4);
            i3 = list3.size();
        }
        if (length <= 0) {
            return "";
        }
        if (length < 4) {
            int[] strToBt = strToBt(str);
            if (str2 != null && str2 != "" && str3 != null && str3 != "" && str4 != null && str4 != "") {
                int[] iArr4 = strToBt;
                for (int i4 = 0; i4 < i; i4++) {
                    iArr4 = enc(iArr4, (int[]) list.get(i4));
                }
                for (int i5 = 0; i5 < i2; i5++) {
                    iArr4 = enc(iArr4, (int[]) list2.get(i5));
                }
                iArr3 = iArr4;
                for (int i6 = 0; i6 < i3; i6++) {
                    iArr3 = enc(iArr3, (int[]) list3.get(i6));
                }
            } else if (str2 != null && str2 != "" && str3 != null && str3 != "") {
                int[] iArr5 = strToBt;
                for (int i7 = 0; i7 < i; i7++) {
                    iArr5 = enc(iArr5, (int[]) list.get(i7));
                }
                iArr3 = iArr5;
                for (int i8 = 0; i8 < i2; i8++) {
                    iArr3 = enc(iArr3, (int[]) list2.get(i8));
                }
            } else if (str2 == null || str2 == "") {
                iArr3 = null;
            } else {
                iArr3 = strToBt;
                for (int i9 = 0; i9 < i; i9++) {
                    iArr3 = enc(iArr3, (int[]) list.get(i9));
                }
            }
            return bt64ToHex(iArr3);
        }
        int i10 = length / 4;
        int i11 = length % 4;
        String str5 = "";
        int i12 = 0;
        while (i12 < i10) {
            int i13 = i12 * 4;
            int[] strToBt2 = strToBt(str.substring(i13 + 0, i13 + 4));
            if (str2 != null && str2 != "" && str3 != null && str3 != "" && str4 != null && str4 != "") {
                iArr2 = strToBt2;
                for (int i14 = 0; i14 < i; i14++) {
                    iArr2 = enc(iArr2, (int[]) list.get(i14));
                }
                for (int i15 = 0; i15 < i2; i15++) {
                    iArr2 = enc(iArr2, (int[]) list2.get(i15));
                }
                for (int i16 = 0; i16 < i3; i16++) {
                    iArr2 = enc(iArr2, (int[]) list3.get(i16));
                }
            } else if (str2 != null && str2 != "" && str3 != null && str3 != "") {
                iArr2 = strToBt2;
                for (int i17 = 0; i17 < i; i17++) {
                    iArr2 = enc(iArr2, (int[]) list.get(i17));
                }
                for (int i18 = 0; i18 < i2; i18++) {
                    iArr2 = enc(iArr2, (int[]) list2.get(i18));
                }
            } else if (str2 == null || str2 == "") {
                iArr2 = null;
            } else {
                iArr2 = strToBt2;
                for (int i19 = 0; i19 < i; i19++) {
                    iArr2 = enc(iArr2, (int[]) list.get(i19));
                }
            }
            str5 = str5 + bt64ToHex(iArr2);
            i12++;
            length = length;
            i10 = i10;
        }
        if (i11 <= 0) {
            return str5;
        }
        int i20 = 0;
        int[] strToBt3 = strToBt(str.substring((i10 * 4) + 0, length));
        if (str2 != null && str2 != "" && str3 != null && str3 != "" && str4 != null && str4 != "") {
            int[] iArr6 = strToBt3;
            for (int i21 = 0; i21 < i; i21++) {
                iArr6 = enc(iArr6, (int[]) list.get(i21));
            }
            for (int i22 = 0; i22 < i2; i22++) {
                iArr6 = enc(iArr6, (int[]) list2.get(i22));
            }
            iArr = iArr6;
            while (i20 < i3) {
                iArr = enc(iArr, (int[]) list3.get(i20));
                i20++;
            }
        } else if (str2 != null && str2 != "" && str3 != null && str3 != "") {
            int[] iArr7 = strToBt3;
            for (int i23 = 0; i23 < i; i23++) {
                iArr7 = enc(iArr7, (int[]) list.get(i23));
            }
            iArr = iArr7;
            while (i20 < i2) {
                iArr = enc(iArr, (int[]) list2.get(i20));
                i20++;
            }
        } else if (str2 == null || str2 == "") {
            iArr = null;
        } else {
            iArr = strToBt3;
            while (i20 < i) {
                iArr = enc(iArr, (int[]) list.get(i20));
                i20++;
            }
        }
        return str5 + bt64ToHex(iArr);
    }

    public String strDec(String str, String str2, String str3, String str4) {
        int i;
        List list;
        int i2;
        List list2;
        int i3;
        List list3;
        int length = str.length();
        if (str2 == null || str2 == "") {
            list = null;
            i = 0;
        } else {
            list = getKeyBytes(str2);
            i = list.size();
        }
        if (str3 == null || str3 == "") {
            list2 = null;
            i2 = 0;
        } else {
            list2 = getKeyBytes(str3);
            i2 = list2.size();
        }
        if (str4 == null || str4 == "") {
            list3 = null;
            i3 = 0;
        } else {
            list3 = getKeyBytes(str4);
            i3 = list3.size();
        }
        String str5 = "";
        int i4 = 0;
        for (int i5 = length / 16; i4 < i5; i5 = i5) {
            int i6 = i4 * 16;
            String hexToBt64 = hexToBt64(str.substring(i6 + 0, i6 + 16));
            int[] iArr = new int[64];
            int i7 = 0;
            for (int i8 = 64; i7 < i8; i8 = 64) {
                int i9 = i7 + 1;
                iArr[i7] = Integer.parseInt(hexToBt64.substring(i7, i9));
                i7 = i9;
            }
            if (str2 != null && str2 != "" && str3 != null && str3 != "" && str4 != null && str4 != "") {
                for (int i10 = i3 - 1; i10 >= 0; i10--) {
                    iArr = dec(iArr, (int[]) list3.get(i10));
                }
                for (int i11 = i2 - 1; i11 >= 0; i11--) {
                    iArr = dec(iArr, (int[]) list2.get(i11));
                }
                for (int i12 = i - 1; i12 >= 0; i12--) {
                    iArr = dec(iArr, (int[]) list.get(i12));
                }
            } else if (str2 != null && str2 != "" && str3 != null && str3 != "") {
                for (int i13 = i2 - 1; i13 >= 0; i13--) {
                    iArr = dec(iArr, (int[]) list2.get(i13));
                }
                for (int i14 = i - 1; i14 >= 0; i14--) {
                    iArr = dec(iArr, (int[]) list.get(i14));
                }
            } else if (str2 == null || str2 == "") {
                iArr = null;
            } else {
                for (int i15 = i - 1; i15 >= 0; i15--) {
                    iArr = dec(iArr, (int[]) list.get(i15));
                }
            }
            str5 = str5 + byteToString(iArr);
            i4++;
        }
        return str5;
    }

    public List getKeyBytes(String str) {
        ArrayList arrayList = new ArrayList();
        int length = str.length();
        int i = length / 4;
        int i2 = length % 4;
        int i3 = 0;
        while (i3 < i) {
            int i4 = i3 * 4;
            arrayList.add(i3, strToBt(str.substring(i4 + 0, i4 + 4)));
            i3++;
        }
        if (i2 > 0) {
            arrayList.add(i3, strToBt(str.substring((i3 * 4) + 0, length)));
        }
        return arrayList;
    }

    public int[] strToBt(String str) {
        int length = str.length();
        int[] iArr = new int[64];
        if (length < 4) {
            for (int i = 0; i < length; i++) {
                char charAt = str.charAt(i);
                for (int i2 = 0; i2 < 16; i2++) {
                    int i3 = 1;
                    for (int i4 = 15; i4 > i2; i4--) {
                        i3 *= 2;
                    }
                    iArr[(i * 16) + i2] = (charAt / i3) % 2;
                }
            }
            while (length < 4) {
                for (int i5 = 0; i5 < 16; i5++) {
                    int i6 = 1;
                    for (int i7 = 15; i7 > i5; i7--) {
                        i6 *= 2;
                    }
                    iArr[(length * 16) + i5] = (0 / i6) % 2;
                }
                length++;
            }
        } else {
            for (int i8 = 0; i8 < 4; i8++) {
                char charAt2 = str.charAt(i8);
                for (int i9 = 0; i9 < 16; i9++) {
                    int i10 = 1;
                    for (int i11 = 15; i11 > i9; i11--) {
                        i10 *= 2;
                    }
                    iArr[(i8 * 16) + i9] = (charAt2 / i10) % 2;
                }
            }
        }
        return iArr;
    }

    public String bt4ToHex(String str) {
        if (str.equalsIgnoreCase("0000")) {
            return "0";
        }
        if (str.equalsIgnoreCase("0001")) {
            return "1";
        }
        if (str.equalsIgnoreCase("0010")) {
            return "2";
        }
        if (str.equalsIgnoreCase("0011")) {
            return "3";
        }
        if (str.equalsIgnoreCase("0100")) {
            return "4";
        }
        if (str.equalsIgnoreCase("0101")) {
            return "5";
        }
        if (str.equalsIgnoreCase("0110")) {
            return "6";
        }
        if (str.equalsIgnoreCase("0111")) {
            return "7";
        }
        if (str.equalsIgnoreCase("1000")) {
            return "8";
        }
        if (str.equalsIgnoreCase("1001")) {
            return "9";
        }
        if (str.equalsIgnoreCase("1010")) {
            return "A";
        }
        if (str.equalsIgnoreCase("1011")) {
            return "B";
        }
        if (str.equalsIgnoreCase("1100")) {
            return "C";
        }
        if (str.equalsIgnoreCase("1101")) {
            return "D";
        }
        if (str.equalsIgnoreCase("1110")) {
            return "E";
        }
        if (str.equalsIgnoreCase("1111")) {
            return "F";
        }
        return "";
    }

    public String hexToBt4(String str) {
        String str2 = "";
        if (str.equalsIgnoreCase("0")) {
            str2 = "0000";
        } else if (str.equalsIgnoreCase("1")) {
            str2 = "0001";
        }
        if (str.equalsIgnoreCase("2")) {
            str2 = "0010";
        }
        if (str.equalsIgnoreCase("3")) {
            str2 = "0011";
        }
        if (str.equalsIgnoreCase("4")) {
            str2 = "0100";
        }
        if (str.equalsIgnoreCase("5")) {
            str2 = "0101";
        }
        if (str.equalsIgnoreCase("6")) {
            str2 = "0110";
        }
        if (str.equalsIgnoreCase("7")) {
            str2 = "0111";
        }
        if (str.equalsIgnoreCase("8")) {
            str2 = "1000";
        }
        if (str.equalsIgnoreCase("9")) {
            str2 = "1001";
        }
        if (str.equalsIgnoreCase("A")) {
            str2 = "1010";
        }
        if (str.equalsIgnoreCase("B")) {
            str2 = "1011";
        }
        if (str.equalsIgnoreCase("C")) {
            str2 = "1100";
        }
        if (str.equalsIgnoreCase("D")) {
            str2 = "1101";
        }
        if (str.equalsIgnoreCase("E")) {
            str2 = "1110";
        }
        return str.equalsIgnoreCase("F") ? "1111" : str2;
    }

    public String byteToString(int[] iArr) {
        String str = "";
        for (int i = 0; i < 4; i++) {
            int i2 = 0;
            for (int i3 = 0; i3 < 16; i3++) {
                int i4 = 1;
                for (int i5 = 15; i5 > i3; i5--) {
                    i4 *= 2;
                }
                i2 += iArr[(i * 16) + i3] * i4;
            }
            if (i2 != 0) {
                str = str + "" + ((char) i2);
            }
        }
        return str;
    }

    public String bt64ToHex(int[] iArr) {
        String str = "";
        for (int i = 0; i < 16; i++) {
            String str2 = "";
            for (int i2 = 0; i2 < 4; i2++) {
                str2 = str2 + iArr[(i * 4) + i2];
            }
            str = str + bt4ToHex(str2);
        }
        return str;
    }

    public String hexToBt64(String str) {
        String str2 = "";
        int i = 0;
        while (i < 16) {
            StringBuilder sb = new StringBuilder();
            sb.append(str2);
            int i2 = i + 1;
            sb.append(hexToBt4(str.substring(i, i2)));
            i = i2;
            str2 = sb.toString();
        }
        return str2;
    }

    public int[] enc(int[] iArr, int[] iArr2) {
        int[][] generateKeys = generateKeys(iArr2);
        int[] initPermute = initPermute(iArr);
        int[] iArr3 = new int[32];
        int[] iArr4 = new int[32];
        int[] iArr5 = new int[32];
        for (int i = 0; i < 32; i++) {
            iArr3[i] = initPermute[i];
            iArr4[i] = initPermute[i + 32];
        }
        for (int i2 = 0; i2 < 16; i2++) {
            for (int i3 = 0; i3 < 32; i3++) {
                iArr5[i3] = iArr3[i3];
                iArr3[i3] = iArr4[i3];
            }
            int[] iArr6 = new int[48];
            for (int i4 = 0; i4 < 48; i4++) {
                iArr6[i4] = generateKeys[i2][i4];
            }
            int[] xor = xor(pPermute(sBoxPermute(xor(expandPermute(iArr4), iArr6))), iArr5);
            for (int i5 = 0; i5 < 32; i5++) {
                iArr4[i5] = xor[i5];
            }
        }
        int[] iArr7 = new int[64];
        for (int i6 = 0; i6 < 32; i6++) {
            iArr7[i6] = iArr4[i6];
            iArr7[i6 + 32] = iArr3[i6];
        }
        return finallyPermute(iArr7);
    }

    public int[] dec(int[] iArr, int[] iArr2) {
        int[][] generateKeys = generateKeys(iArr2);
        int[] initPermute = initPermute(iArr);
        int[] iArr3 = new int[32];
        int[] iArr4 = new int[32];
        int[] iArr5 = new int[32];
        for (int i = 0; i < 32; i++) {
            iArr3[i] = initPermute[i];
            iArr4[i] = initPermute[i + 32];
        }
        for (int i2 = 15; i2 >= 0; i2--) {
            for (int i3 = 0; i3 < 32; i3++) {
                iArr5[i3] = iArr3[i3];
                iArr3[i3] = iArr4[i3];
            }
            int[] iArr6 = new int[48];
            for (int i4 = 0; i4 < 48; i4++) {
                iArr6[i4] = generateKeys[i2][i4];
            }
            int[] xor = xor(pPermute(sBoxPermute(xor(expandPermute(iArr4), iArr6))), iArr5);
            for (int i5 = 0; i5 < 32; i5++) {
                iArr4[i5] = xor[i5];
            }
        }
        int[] iArr7 = new int[64];
        for (int i6 = 0; i6 < 32; i6++) {
            iArr7[i6] = iArr4[i6];
            iArr7[i6 + 32] = iArr3[i6];
        }
        return finallyPermute(iArr7);
    }

    public int[] initPermute(int[] iArr) {
        int[] iArr2 = new int[64];
        int i = 0;
        int i2 = 1;
        int i3 = 0;
        while (i < 4) {
            int i4 = 7;
            int i5 = 0;
            while (i4 >= 0) {
                int i6 = (i * 8) + i5;
                int i7 = i4 * 8;
                iArr2[i6] = iArr[i7 + i2];
                iArr2[i6 + 32] = iArr[i7 + i3];
                i4--;
                i5++;
            }
            i++;
            i2 += 2;
            i3 += 2;
        }
        return iArr2;
    }

    public int[] expandPermute(int[] iArr) {
        int[] iArr2 = new int[48];
        for (int i = 0; i < 8; i++) {
            if (i == 0) {
                iArr2[(i * 6) + 0] = iArr[31];
            } else {
                iArr2[(i * 6) + 0] = iArr[(i * 4) - 1];
            }
            int i2 = i * 6;
            int i3 = i * 4;
            iArr2[i2 + 1] = iArr[i3 + 0];
            iArr2[i2 + 2] = iArr[i3 + 1];
            iArr2[i2 + 3] = iArr[i3 + 2];
            iArr2[i2 + 4] = iArr[i3 + 3];
            if (i == 7) {
                iArr2[i2 + 5] = iArr[0];
            } else {
                iArr2[i2 + 5] = iArr[i3 + 4];
            }
        }
        return iArr2;
    }

    public int[] xor(int[] iArr, int[] iArr2) {
        int[] iArr3 = new int[iArr.length];
        for (int i = 0; i < iArr.length; i++) {
            iArr3[i] = iArr[i] ^ iArr2[i];
        }
        return iArr3;
    }

    public int[] sBoxPermute(int[] iArr) {
        int[] iArr2 = new int[32];
        int i = 0;
        int i2 = 1;
        int[][] iArr3 = {new int[]{14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7}, new int[]{0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8}, new int[]{4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0}, new int[]{15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13}};
        int[][] iArr4 = {new int[]{15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10}, new int[]{3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5}, new int[]{0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15}, new int[]{13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9}};
        int[][] iArr5 = {new int[]{10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8}, new int[]{13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1}, new int[]{13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7}, new int[]{1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12}};
        int[][] iArr6 = {new int[]{7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15}, new int[]{13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9}, new int[]{10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4}, new int[]{3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14}};
        int[][] iArr7 = {new int[]{2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9}, new int[]{14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6}, new int[]{4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14}, new int[]{11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3}};
        int[][] iArr8 = {new int[]{12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11}, new int[]{10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8}, new int[]{9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6}, new int[]{4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13}};
        int[][] iArr9 = {new int[]{4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1}, new int[]{13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6}, new int[]{1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2}, new int[]{6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12}};
        int[][] iArr10 = {new int[]{13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7}, new int[]{1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2}, new int[]{7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8}, new int[]{2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11}};
        String str = "";
        int i3 = 0;
        while (i3 < 8) {
            int i4 = i3 * 6;
            int i5 = (iArr[i4 + 0] * 2) + iArr[i4 + 5];
            int i6 = (iArr[i4 + 1] * 2 * 2 * 2) + (iArr[i4 + 2] * 2 * 2) + (iArr[i4 + 3] * 2) + iArr[i4 + 4];
            switch (i3) {
                case 0:
                    str = getBoxBinary(iArr3[i5][i6]);
                    break;
                case 1:
                    str = getBoxBinary(iArr4[i5][i6]);
                    break;
                case 2:
                    str = getBoxBinary(iArr5[i5][i6]);
                    break;
                case 3:
                    str = getBoxBinary(iArr6[i5][i6]);
                    break;
                case 4:
                    str = getBoxBinary(iArr7[i5][i6]);
                    break;
                case 5:
                    str = getBoxBinary(iArr8[i5][i6]);
                    break;
                case 6:
                    str = getBoxBinary(iArr9[i5][i6]);
                    break;
                case 7:
                    str = getBoxBinary(iArr10[i5][i6]);
                    break;
            }
            int i7 = i3 * 4;
            iArr2[i7 + 0] = Integer.parseInt(str.substring(i, i2));
            iArr2[i7 + 1] = Integer.parseInt(str.substring(i2, 2));
            iArr2[i7 + 2] = Integer.parseInt(str.substring(2, 3));
            iArr2[i7 + 3] = Integer.parseInt(str.substring(3, 4));
            i3++;
            i = 0;
            i2 = 1;
        }
        return iArr2;
    }

    public int[] pPermute(int[] iArr) {
        return new int[]{iArr[15], iArr[6], iArr[19], iArr[20], iArr[28], iArr[11], iArr[27], iArr[16], iArr[0], iArr[14], iArr[22], iArr[25], iArr[4], iArr[17], iArr[30], iArr[9], iArr[1], iArr[7], iArr[23], iArr[13], iArr[31], iArr[26], iArr[2], iArr[8], iArr[18], iArr[12], iArr[29], iArr[5], iArr[21], iArr[10], iArr[3], iArr[24]};
    }

    public int[] finallyPermute(int[] iArr) {
        return new int[]{iArr[39], iArr[7], iArr[47], iArr[15], iArr[55], iArr[23], iArr[63], iArr[31], iArr[38], iArr[6], iArr[46], iArr[14], iArr[54], iArr[22], iArr[62], iArr[30], iArr[37], iArr[5], iArr[45], iArr[13], iArr[53], iArr[21], iArr[61], iArr[29], iArr[36], iArr[4], iArr[44], iArr[12], iArr[52], iArr[20], iArr[60], iArr[28], iArr[35], iArr[3], iArr[43], iArr[11], iArr[51], iArr[19], iArr[59], iArr[27], iArr[34], iArr[2], iArr[42], iArr[10], iArr[50], iArr[18], iArr[58], iArr[26], iArr[33], iArr[1], iArr[41], iArr[9], iArr[49], iArr[17], iArr[57], iArr[25], iArr[32], iArr[0], iArr[40], iArr[8], iArr[48], iArr[16], iArr[56], iArr[24]};
    }

    public int[][] generateKeys(int[] iArr) {
        int[] iArr2 = new int[56];
        int[][] iArr3 = (int[][]) Array.newInstance(int.class, 16, 48);
        int[] iArr4 = {1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1};
        for (int i = 0; i < 7; i++) {
            int i2 = 0;
            int i3 = 7;
            while (i2 < 8) {
                iArr2[(i * 8) + i2] = iArr[(i3 * 8) + i];
                i2++;
                i3--;
            }
        }
        for (int i4 = 0; i4 < 16; i4++) {
            for (int i5 = 0; i5 < iArr4[i4]; i5++) {
                int i6 = iArr2[0];
                int i7 = iArr2[28];
                int i8 = 0;
                while (i8 < 27) {
                    int i9 = i8 + 1;
                    iArr2[i8] = iArr2[i9];
                    iArr2[i8 + 28] = iArr2[i8 + 29];
                    i8 = i9;
                }
                iArr2[27] = i6;
                iArr2[55] = i7;
            }
            int[] iArr5 = {iArr2[13], iArr2[16], iArr2[10], iArr2[23], iArr2[0], iArr2[4], iArr2[2], iArr2[27], iArr2[14], iArr2[5], iArr2[20], iArr2[9], iArr2[22], iArr2[18], iArr2[11], iArr2[3], iArr2[25], iArr2[7], iArr2[15], iArr2[6], iArr2[26], iArr2[19], iArr2[12], iArr2[1], iArr2[40], iArr2[51], iArr2[30], iArr2[36], iArr2[46], iArr2[54], iArr2[29], iArr2[39], iArr2[50], iArr2[44], iArr2[32], iArr2[47], iArr2[43], iArr2[48], iArr2[38], iArr2[55], iArr2[33], iArr2[52], iArr2[45], iArr2[41], iArr2[49], iArr2[35], iArr2[28], iArr2[31]};
            switch (i4) {
                case 0:
                    for (int i10 = 0; i10 < 48; i10++) {
                        iArr3[0][i10] = iArr5[i10];
                    }
                    break;
                case 1:
                    for (int i11 = 0; i11 < 48; i11++) {
                        iArr3[1][i11] = iArr5[i11];
                    }
                    break;
                case 2:
                    for (int i12 = 0; i12 < 48; i12++) {
                        iArr3[2][i12] = iArr5[i12];
                    }
                    break;
                case 3:
                    for (int i13 = 0; i13 < 48; i13++) {
                        iArr3[3][i13] = iArr5[i13];
                    }
                    break;
                case 4:
                    for (int i14 = 0; i14 < 48; i14++) {
                        iArr3[4][i14] = iArr5[i14];
                    }
                    break;
                case 5:
                    for (int i15 = 0; i15 < 48; i15++) {
                        iArr3[5][i15] = iArr5[i15];
                    }
                    break;
                case 6:
                    for (int i16 = 0; i16 < 48; i16++) {
                        iArr3[6][i16] = iArr5[i16];
                    }
                    break;
                case 7:
                    for (int i17 = 0; i17 < 48; i17++) {
                        iArr3[7][i17] = iArr5[i17];
                    }
                    break;
                case 8:
                    for (int i18 = 0; i18 < 48; i18++) {
                        iArr3[8][i18] = iArr5[i18];
                    }
                    break;
                case 9:
                    for (int i19 = 0; i19 < 48; i19++) {
                        iArr3[9][i19] = iArr5[i19];
                    }
                    break;
                case 10:
                    for (int i20 = 0; i20 < 48; i20++) {
                        iArr3[10][i20] = iArr5[i20];
                    }
                    break;
                case 11:
                    for (int i21 = 0; i21 < 48; i21++) {
                        iArr3[11][i21] = iArr5[i21];
                    }
                    break;
                case 12:
                    for (int i22 = 0; i22 < 48; i22++) {
                        iArr3[12][i22] = iArr5[i22];
                    }
                    break;
                case 13:
                    for (int i23 = 0; i23 < 48; i23++) {
                        iArr3[13][i23] = iArr5[i23];
                    }
                    break;
                case 14:
                    for (int i24 = 0; i24 < 48; i24++) {
                        iArr3[14][i24] = iArr5[i24];
                    }
                    break;
                case 15:
                    for (int i25 = 0; i25 < 48; i25++) {
                        iArr3[15][i25] = iArr5[i25];
                    }
                    break;
            }
        }
        return iArr3;
    }
}
