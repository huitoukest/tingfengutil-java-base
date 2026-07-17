package com.tingfeng.util.java.base.crypto;

import com.tingfeng.util.java.base.common.constant.EncryptionAlgorithmType;
import com.tingfeng.util.java.base.lang.exception.BaseException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 基于哈希映射的字符替换编码策略实现。
 * <p>
 * 使用 64 个基础字符（0-9, A-Z, a-z, _, -）进行确定性映射编码，
 * 适用于 ID 混淆等场景，不适用于安全加密（可被频率分析破解）。
 * </p>
 * <p>
 * <b>安全边界说明：</b>
 * - 此为字符替换编码（substitution cipher），不是真正的加密算法
 * - 通过哈希映射打乱字符顺序实现编码，安全性较低
 * - 可能被频率分析攻击破解
 * - 仅适用于：用户可见 ID 的混淆、非敏感数据的编码、防止 URL 参数被直接猜测
 * - 不适用于：保护敏感数据、密码、令牌、个人身份信息等
 * </p>
 * <p>
 * <b>字符集限制：</b>仅支持 64 个字符：0-9, A-Z, a-z, _, -。
 * 严格模式下遇到非法字符抛出 {@link IllegalArgumentException}；
 * 宽松模式下非法字符保持原样不变。
 * </p>
 * <p>
 * <b>线程安全说明：</b>构造函数完成后，所有只读操作是线程安全的。
 * {@link #hashPositionDictionary(int)} 需要外部同步，已通过 synchronized 保护。
 * </p>
 * <p>
 * <b>密钥说明：</b>{@link #encrypt(byte[], byte[])} 和 {@link #decrypt(byte[], byte[])}
 * 的 key 参数在此策略中被忽略，因为编码映射由构造时传入的 salt 决定。
 * </p>
 * <p>
 * <b>位置偏移编码：</b>可调用 {@link #hashPositionDictionary(int)} 启用基于字符位置的偏移编码，
 * 提高混淆程度。调用后自动启用位置偏移模式。
 * </p>
 *
 * @author huitoukest
 * @see EncryptionStrategy
 */
public class HashStrategy implements EncryptionStrategy {

    /**
     * 操作模式枚举。
     */
    public enum Mode {
        /** 严格模式：遇到非法字符抛出 IllegalArgumentException */
        STRICT,
        /** 宽松模式：遇到非法字符保持原样不变 */
        LOOSE
    }

    /**
     * 64 个基础字符：0-9, A-Z, a-z, _, -
     */
    private static final char[] BASE_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_-".toCharArray();

    /**
     * 字符索引字典，大小为 256（覆盖 ASCII 扩展范围）。
     * 索引 = 字符的 ASCII 码，值 = 该字符在 {@link #BASE_CHARS} 中的索引。
     * 不在基础字符集中的字符对应值为 -1。
     */
    private static final int[] BASE_CHARS_DICTIONARY = new int[256];

    static {
        Arrays.fill(BASE_CHARS_DICTIONARY, -1);
        for (int i = 0; i < BASE_CHARS.length; i++) {
            BASE_CHARS_DICTIONARY[BASE_CHARS[i]] = i;
        }
    }

    /**
     * 打乱后的字符映射表，与 {@link #BASE_CHARS} 一一映射。
     */
    private char[] hashedChars = null;

    /**
     * 打乱后字符的索引字典，大小为 256。
     * 索引 = 字符的 ASCII 码，值 = 该字符在 {@link #hashedChars} 中的索引。
     * 不在映射字符集中的字符对应值为 -1。
     */
    private final int[] hashedCharsDictionary = new int[256];

    /**
     * 映射加盐字符串
     */
    private String salt;

    /**
     * 基于位置偏移的映射字典，使用 volatile 保证可见性。
     */
    private volatile int[] hashedPositionValueOffsetDictionary;

    /**
     * 操作模式
     */
    private final Mode mode;

    private boolean withPositionEncode = false;

    /**
     * 使用指定盐值构造实例，默认严格模式。
     *
     * @param salt 盐值字符串，为空或 null 时使用空字符串
     */
    public HashStrategy(String salt) {
        this(salt, Mode.STRICT);
    }

    /**
     * 使用指定盐值和操作模式构造实例。
     *
     * @param salt 盐值字符串，为空或 null 时使用空字符串
     * @param mode 操作模式，不能为 null
     * @throws IllegalArgumentException 如果 mode 为 null
     */
    public HashStrategy(String salt, Mode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("Mode must not be null");
        }
        this.mode = mode;
        if (null == salt || salt.isEmpty()) {
            salt = "";
        }
        this.salt = salt + salt.length();
        hashChars();
    }

    /**
     * 初始化位置偏移映射字典并启用位置偏移编码。
     * <p>
     * 调用此方法后，加密/解码操作将使用基于字符位置的偏移编码，
     * 提高混淆程度。每次编码/解码时根据字符位置应用不同的偏移量。
     * </p>
     *
     * @param length 字典长度，必须大于 0
     */
    public synchronized void hashPositionDictionary(int length) {
        assert length > 0;
        hashedPositionValueOffsetDictionary = new int[length];
        int saltLength = this.salt.length();
        long seedValue = saltLength;
        char[] saltArr = salt.toCharArray();
        for (int i = 0; i < saltArr.length; i++) {
            int v = ((int) saltArr[i]);
            seedValue += v * 13;
        }
        seedValue = Math.abs(seedValue);
        for (int i = 0; i < hashedPositionValueOffsetDictionary.length; i++) {
            long modValue = saltLength > i ? seedValue + saltArr[i] : seedValue + saltArr[i % saltLength] + i;
            int offsetValue = (int) (modValue % BASE_CHARS.length);
            hashedPositionValueOffsetDictionary[i] = offsetValue;
        }
        this.withPositionEncode = true;
    }

    /**
     * 检查是否已启用位置偏移编码。
     *
     * @return true 表示已启用位置偏移编码
     */
    public boolean isWithPositionEncode() {
        return withPositionEncode;
    }

    /**
     * 根据盐值将基础字符顺序打乱，生成映射表。
     */
    private void hashChars() {
        long seedValue = 0;
        char[] saltArr = salt.toCharArray();
        for (int i = 0; i < saltArr.length; i++) {
            int v = ((int) saltArr[i]);
            seedValue += v * 31;
        }
        seedValue += saltArr.length;
        seedValue = Math.abs(seedValue);

        hashedChars = new char[BASE_CHARS.length];
        System.arraycopy(BASE_CHARS, 0, hashedChars, 0, BASE_CHARS.length);

        // 洗牌打乱顺序
        for (int i = 0; i < BASE_CHARS.length; i++) {
            long modValue = saltArr.length > i ? seedValue + saltArr[i] : seedValue + i;
            int exChangeIdx = (int) (modValue % BASE_CHARS.length);
            char tmp = hashedChars[i];
            hashedChars[i] = hashedChars[exChangeIdx];
            hashedChars[exChangeIdx] = tmp;
        }

        // 初始化 hashedCharsDictionary，未映射字符对应 -1
        Arrays.fill(hashedCharsDictionary, -1);
        for (int i = 0; i < hashedChars.length; i++) {
            hashedCharsDictionary[hashedChars[i]] = i;
        }
    }

    /**
     * 将原始字符串编码，可选是否启用位置偏移编码。
     *
     * @param str                需要编码的字符串
     * @param withPositionEncode 是否启用位置偏移编码
     * @return 编码后的字符串
     * @throws IllegalArgumentException 如果 str 为 null，或在严格模式下包含非法字符
     */
    String encode(String str, boolean withPositionEncode) {
        if (str == null) {
            throw new IllegalArgumentException("Input string must not be null");
        }
        if (str.isEmpty()) {
            return "";
        }
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            // 非 ASCII 字符（>255）无法在字典中查找
            int index;
            if (c > 255) {
                if (mode == Mode.STRICT) {
                    throw new IllegalArgumentException("Illegal character '" + c + "' at position " + i + ": not in BASE_CHARS set");
                }
                // LOOSE 模式：保持原样
                continue;
            }
            index = BASE_CHARS_DICTIONARY[c];
            if (index == -1) {
                if (mode == Mode.STRICT) {
                    throw new IllegalArgumentException("Illegal character '" + c + "' at position " + i + ": not in BASE_CHARS set");
                }
                // LOOSE 模式：保持原样
                continue;
            }
            if (withPositionEncode) {
                checkPositionDicIsInit();
                int offsetValue = hashedPositionValueOffsetDictionary[i % hashedPositionValueOffsetDictionary.length];
                index = index + (offsetValue + str.length() % BASE_CHARS.length);
                index = index % BASE_CHARS.length;
            }
            chars[i] = hashedChars[index];
        }
        return new String(chars);
    }

    /**
     * 将编码后的字符串解码，可选是否启用位置偏移解码。
     *
     * @param str                需要解码的字符串
     * @param withPositionDecode 是否启用位置偏移解码
     * @return 解码后的字符串
     * @throws IllegalArgumentException 如果 str 为 null，或在严格模式下包含非法字符
     */
    String decode(String str, boolean withPositionDecode) {
        if (str == null) {
            throw new IllegalArgumentException("Input string must not be null");
        }
        if (str.isEmpty()) {
            return "";
        }
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            int index;
            if (c > 255) {
                if (mode == Mode.STRICT) {
                    throw new IllegalArgumentException("Illegal character '" + c + "' at position " + i + ": not in encoded character set");
                }
                // LOOSE 模式：保持原样
                continue;
            }
            index = hashedCharsDictionary[c];
            if (index == -1) {
                if (mode == Mode.STRICT) {
                    throw new IllegalArgumentException("Illegal character '" + c + "' at position " + i + ": not in encoded character set");
                }
                // LOOSE 模式：保持原样
                continue;
            }
            if (withPositionDecode) {
                checkPositionDicIsInit();
                int offsetValue = hashedPositionValueOffsetDictionary[i % hashedPositionValueOffsetDictionary.length];
                index = index - (offsetValue + str.length()) % BASE_CHARS.length;
                if (index < 0) {
                    index = index % BASE_CHARS.length + BASE_CHARS.length;
                }
            }
            chars[i] = BASE_CHARS[index];
        }
        return new String(chars);
    }

    /**
     * 检查位置偏移字典是否已初始化。
     *
     * @throws BaseException 如果位置偏移字典未初始化
     */
    private void checkPositionDicIsInit() {
        if (hashedPositionValueOffsetDictionary == null) {
            throw new BaseException("you must invoke hashPositionDictionary() method to init  position dic data");
        }
    }

    @Override
    public byte[] encrypt(byte[] data, byte[] key) {
        if (null == data) {
            return null;
        }
        String input = new String(data, StandardCharsets.UTF_8);
        String encoded = encode(input, withPositionEncode);
        return encoded.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] decrypt(byte[] data, byte[] key) {
        if (null == data) {
            return null;
        }
        String input = new String(data, StandardCharsets.UTF_8);
        String decoded = decode(input, withPositionEncode);
        return decoded.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public EncryptionAlgorithmType getType() {
        return EncryptionAlgorithmType.SUBSTITUTION;
    }

    @Override
    public boolean supportsDecrypt() {
        return true;
    }
}
