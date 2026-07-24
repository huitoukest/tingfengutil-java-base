package com.tingfeng.util.java.base.gis.op;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * EGM96 大地水准面数据引擎
 *
 * 负责 EGM96 格网数据的加载、存储和双线性插值计算。
 * 支持从二进制数据文件加载，也提供简化球谐近似作为默认回退。
 *
 * 数据文件格式（big-endian）：
 * - Magic: 4 bytes = 0x45474D39 ('EGM9')
 * - Version: 2 bytes, unsigned short = 1
 * - LatCount: 2 bytes, unsigned short = 181
 * - LonCount: 2 bytes, unsigned short = 361
 * - Data: LatCount * LonCount * 2 bytes, signed short in cm
 *
 * 纬度索引约定：i=0 = 90°N, i=180 = 90°S，与插值公式 (90 - lat) 一致。
 * 经度索引约定：j=0 = 0°, j=360 = 360°，经度方向采用环绕寻址。
 *
 * 线程安全：通过 synchronized 延迟加载实现线程安全的单例访问。
 *
 * 默认回退：当 classpath 下数据文件不存在时，使用简化的低阶球谐函数
 * 生成合成格网数据，确保框架始终可用。合成数据的幅度约为 ±50 米，
 * 反映真实 EGM96 的大尺度特征。
 */
public final class EGM96Grid {

    /**
     * 纬度格点数：90°S 到 90°N，1° 步长
     */
    public static final int LAT_COUNT = 181;

    /**
     * 经度格点数：0° 到 360°，1° 步长
     */
    public static final int LON_COUNT = 361;

    /**
     * 数据文件路径（classpath）
     */
    private static final String DATA_FILE_PATH = "gis/op/egm96-1deg.dat";

    /**
     * 数据文件魔数 'EGM9'
     */
    private static final int MAGIC = 0x45474D39;

    /**
     * 受支持的数据文件版本
     */
    private static final int SUPPORTED_VERSION = 1;

    /**
     * 合成格网的最大振幅（米），用于生成默认回退数据
     */
    private static final double SYNTHETIC_AMPLITUDE = 50.0;

    /**
     * 延迟加载的单例实例
     */
    private static volatile EGM96Grid instance;

    /**
     * 格网高度数据，单位为厘米（short 类型）
     * 存储格式：一维数组，索引 = latIndex * LON_COUNT + lonIndex
     */
    private final short[] heights;

    /**
     * 数据来源描述
     */
    private final String source;

    /**
     * 获取 EGM96Grid 单例实例
     *
     * 首次调用时尝试从 classpath 加载数据文件，文件不存在时自动使用
     * 合成格网数据作为回退。后续调用返回缓存的实例。
     *
     * @return EGM96Grid 单例实例
     */
    public static synchronized EGM96Grid getInstance() {
        if (instance == null) {
            instance = loadFromClasspath();
        }
        return instance;
    }

    /**
     * 从 classpath 加载数据文件，失败时使用合成格网回退
     *
     * @return 加载成功的 EGM96Grid 实例
     */
    private static EGM96Grid loadFromClasspath() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = EGM96Grid.class.getClassLoader();
        }
        try (InputStream is = cl.getResourceAsStream(DATA_FILE_PATH)) {
            if (is != null) {
                return load(is);
            }
        } catch (IOException e) {
            // 文件读取失败，使用合成数据回退
        }
        return createSynthetic();
    }

    /**
     * 从输入流加载 EGM96 格网数据
     *
     * @param input 包含 EGM96 二进制数据的输入流，不可为 null
     * @return 加载完成的 EGM96Grid 实例
     * @throws IOException 如果数据格式无效或读取失败
     * @throws IllegalArgumentException 如果 input 为 null
     */
    public static EGM96Grid load(InputStream input) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("Input stream must not be null");
        }
        DataInputStream dis = new DataInputStream(new BufferedInputStream(input));

        // 读取魔数
        int magic = dis.readInt();
        if (magic != MAGIC) {
            throw new IOException("Invalid EGM96 data file: bad magic number 0x"
                    + Integer.toHexString(magic));
        }

        // 读取版本号
        int version = dis.readUnsignedShort();
        if (version != SUPPORTED_VERSION) {
            throw new IOException("Unsupported EGM96 data file version: " + version
                    + ", expected: " + SUPPORTED_VERSION);
        }

        // 读取格网尺寸
        int latCount = dis.readUnsignedShort();
        int lonCount = dis.readUnsignedShort();
        if (latCount != LAT_COUNT || lonCount != LON_COUNT) {
            throw new IOException("Invalid EGM96 grid dimensions: "
                    + latCount + "x" + lonCount
                    + ", expected: " + LAT_COUNT + "x" + LON_COUNT);
        }

        // 读取高度数据
        int total = latCount * lonCount;
        short[] heights = new short[total];
        for (int i = 0; i < total; i++) {
            heights[i] = dis.readShort();
        }

        return new EGM96Grid(heights, "file");
    }

    /**
     * 使用简化球谐函数创建合成格网数据
     *
     * 生成原理：使用几个低阶球谐项（主要反映 J2 扁率项和几个低阶非带谐项）
     * 近似大地水准面的大尺度特征。合成数据的幅度控制在约 ±50 米。
     *
     * 球谐公式包含：
     * - J2 带谐项（纬向 2 阶）：大地水准面主要的长轴 bulge
     * - (2,1) 扇谐项：主要的经向不对称
     * - (3,1) 田谐项：中纬度波动
     * - J4 带谐项：四极修正
     *
     * @return 包含合成数据的 EGM96Grid 实例
     */
    public static EGM96Grid createSynthetic() {
        short[] heights = new short[LAT_COUNT * LON_COUNT];
        for (int i = 0; i < LAT_COUNT; i++) {
            // 纬度：i=0 -> 90°N, i=180 -> -90°S
            double lat = 90.0 - i;
            double sinLat = Math.sin(Math.toRadians(lat));
            double cosLat = Math.cos(Math.toRadians(lat));
            double sinLat2 = sinLat * sinLat;
            double sinLat4 = sinLat2 * sinLat2;

            for (int j = 0; j < LON_COUNT; j++) {
                // 经度：j=0 -> 0°, j=360 -> 360°
                double lon = j;
                double lonRad = Math.toRadians(lon);
                double cosLon = Math.cos(lonRad);
                double sinLon = Math.sin(lonRad);

                // 简化球谐近似：几项主导的低阶项
                double height = SYNTHETIC_AMPLITUDE * (
                        // J2 带谐项 P2,0（赤道 bulge）
                        -0.6 * (3.0 * sinLat2 - 1.0) / 2.0
                        // (2,1) 扇谐项 C21,S21（经向不对称）
                        + 0.4 * sinLat * cosLat * cosLon
                        // (3,1) 田谐项
                        + 0.3 * sinLat * (5.0 * sinLat2 - 3.0) / 2.0 * sinLon
                        // J4 带谐项 P4,0（中纬度波动）
                        - 0.25 * (35.0 * sinLat4 - 30.0 * sinLat2 + 3.0) / 8.0
                );

                // 转换为厘米并存储为 short
                heights[i * LON_COUNT + j] = (short) Math.round(height * 100.0);
            }
        }
        return new EGM96Grid(heights, "synthetic");
    }

    /**
     * 私有构造器，通过静态工厂方法创建实例
     *
     * @param heights 格网高度数据（厘米）
     * @param source  数据来源描述
     */
    private EGM96Grid(short[] heights, String source) {
        this.heights = heights;
        this.source = source;
    }

    /**
     * 获取数据来源描述
     *
     * @return 数据来源，如 "file" 或 "synthetic"
     */
    public String getSource() {
        return source;
    }

    /**
     * 获取指定格网点的原始高度值
     *
     * @param latIndex 纬度索引（0=90°N, 180=90°S）
     * @param lonIndex 经度索引（0=0°, 360=360°）
     * @return 格网点高度值，单位：厘米
     * @throws IndexOutOfBoundsException 如果索引越界
     */
    public short getRawHeight(int latIndex, int lonIndex) {
        return heights[latIndex * LON_COUNT + lonIndex];
    }

    /**
     * 计算指定经纬度处的大地水准面高度（双线性插值）
     *
     * 使用标准双线性插值方法，在最近的四个格网点之间进行插值。
     *
     * 插值公式：
     * <pre>
     * H(lat, lon) = (1-t)*(1-u)*h00 + (1-t)*u*h01 + t*(1-u)*h10 + t*u*h11
     * </pre>
     *
     * 其中：
     * - i0 = floor(90 - lat)，对应左下角纬度格网索引
     * - j0 = floor(lon % 360)，对应左下角经度格网索引
     * - t = (90 - lat) - i0，纬度方向插值权重（0 到 1）
     * - u = lon - j0，经度方向插值权重（0 到 1）
     *
     * 边界处理：
     * - 经度方向采用环绕寻址，j1 = (j0 + 1) % LON_COUNT
     * - 纬度方向 clamp 到有效范围，确保 i0 不超过 LAT_COUNT - 2
     * - 纬度范围限制在 [-90, 90]，超出时 clamp
     *
     * @param lat 纬度（度），范围 [-90, 90]
     * @param lon 经度（度），范围 [-180, 180] 或 [0, 360]
     * @return 大地水准面高度，单位：米
     */
    public double getGeoidHeight(double lat, double lon) {
        // 将经度归一化到 [0, 360)
        double normalizedLon = lon % 360.0;
        if (normalizedLon < 0) {
            normalizedLon += 360.0;
        }

        // 将纬度限制到 [-90, 90]
        double normalizedLat = Math.max(-90.0, Math.min(90.0, lat));

        // 计算纬度方向的格网索引
        double latPos = 90.0 - normalizedLat;
        int i0 = (int) Math.floor(latPos);
        // clamp 到有效范围，确保 i1 = i0 + 1 不越界
        i0 = Math.max(0, Math.min(i0, LAT_COUNT - 2));
        int i1 = i0 + 1;

        // 计算经度方向的格网索引
        int j0 = (int) Math.floor(normalizedLon);
        if (j0 >= LON_COUNT - 1) {
            j0 = LON_COUNT - 2;
        }
        int j1 = (j0 + 1) % LON_COUNT;

        // 插值权重
        double t = latPos - i0;
        double u = normalizedLon - j0;

        // 四个格网点的高度值（厘米转米）
        double h00 = heights[i0 * LON_COUNT + j0] / 100.0;
        double h01 = heights[i0 * LON_COUNT + j1] / 100.0;
        double h10 = heights[i1 * LON_COUNT + j0] / 100.0;
        double h11 = heights[i1 * LON_COUNT + j1] / 100.0;

        // 标准双线性插值
        return (1.0 - t) * (1.0 - u) * h00
                + (1.0 - t) * u * h01
                + t * (1.0 - u) * h10
                + t * u * h11;
    }
}
