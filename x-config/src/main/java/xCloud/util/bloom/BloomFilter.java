package xCloud.util.bloom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/6/13 17:18
 * @ClassName BloomFilter 布隆过滤器是一种空间高效的概率性数据结构，用于快速判断一个元素是否可能存在于集合中，可能有一定误判率（假阳性），但不会漏判（假阴性）。下面是一个简单的 Java 实现，包括核心功能和注释说明：
 */
@Data
//@NoArgsConstructor(force = true)
@AllArgsConstructor
public class BloomFilter {

    private final BitSet bitSet;
    private final int size;
    private final int hashFunctions;
    private final int[] seeds;

    /**
     * This class implements a vector of bits that grows as needed. Each component of the bit set has a boolean value. The bits of a BitSet are indexed by nonnegative integers. Individual indexed bits can be examined, set, or cleared. One BitSet may be used to modify the contents of another BitSet through logical AND, logical inclusive OR, and logical exclusive OR operations.
     * By default, all bits in the set initially have the value false.
     * Every bit set has a current size, which is the number of bits of space currently in use by the bit set. Note that the size is related to the implementation of a bit set, so it may change with implementation. The length of a bit set relates to logical length of a bit set and is defined independently of implementation.
     * Unless otherwise noted, passing a null parameter to any of the methods in a BitSet will result in a NullPointerException.
     * A BitSet is not safe for multithreaded use without external synchronization.
     * 这个类实现了一个根据需要增长的位向量。位集的每个分量都有一个布尔值。BitSet的位由非负整数索引。可以检查、设置或清除单个索引位。一个BitSet可用于通过逻辑AND、逻辑包含OR和逻辑异或操作修改另一个BitSet的内容。
     * 默认情况下，集合中的所有位最初都具有值false。
     * 每个比特集都有一个当前大小，即比特集当前使用的空间比特数。请注意，大小与位集的实现有关，因此它可能会随着实现而变化。比特集的长度与比特集的逻辑长度有关，并且独立于实现而定义。
     * 除非另有说明，否则向BitSet中的任何方法传递null参数都会导致NullPointerException。
     * 如果没有外部同步，BitSet对于多线程使用是不安全的。
     *
     * @param expectInsertions
     * @param falsePositiveRate
     */
    public BloomFilter(int expectInsertions, double falsePositiveRate) {
        //根据公式计算位数组大小：m=-n*log(p)/（ln(2)）^2
        this.size = optimalSize(expectInsertions, falsePositiveRate);
        //计算哈希函数个数：k=ln(2)*m/n
        this.hashFunctions = optimalHashFunctions(expectInsertions, size);
        this.bitSet = new BitSet(size);
        this.seeds = new int[hashFunctions];
        //初始化种子，用于生成不同的哈希函数
        for (int i = 0; i < hashFunctions; i++) {
            seeds[i] = i + 1;
        }
    }

    //添加元素到布隆过滤器
    public void add(String value) {
        for (int seed : seeds) {
            int index = hash(value, seed);
            bitSet.set(index);
        }
    }

    //检测元素是否可能存在集合中
    public boolean mightContain(String value) {
        for (int seed : seeds) {
            int index = hash(value, seed);
            if (!bitSet.get(index)) {
                return false;
            }
        }
        return true;//所有位都为1，元素可能存在集合中
    }

    //计算位数组大小
    private int optimalSize(long n, double p) {
        return (int) (-n * Math.log(p) / (Math.log(2) * Math.log(2)));
    }

    //计算哈希函数个数
    private int optimalHashFunctions(long n, long m) {
        return Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
    }

    //自定义哈希函数
    private int hash(String value, int seed) {
        int hash = 0;
        for (char c : value.toCharArray()) {
            hash = seed * hash + c;
        }
        return Math.abs(hash & size);
    }

    public static String getHash(String input, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        byte[] digest = messageDigest.digest(input.getBytes(StandardCharsets.UTF_8));
        //将字节数组转为十六进制字符串
        StringBuilder hexString = new StringBuilder();
        for (byte b : digest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        BloomFilter bloomFilter = new BloomFilter(1000, 0.01);
        bloomFilter.add("hello");
        bloomFilter.add("apple");
        bloomFilter.add("banana");
        System.out.println(bloomFilter.mightContain("hello"));
        System.out.println(bloomFilter.mightContain("apple"));
        System.out.println(bloomFilter.mightContain("banana"));
        System.out.println(bloomFilter.mightContain("orange"));
        String sk = "樊迎宾";
        System.out.println(sk.hashCode());
        System.out.println(getHash(sk, "MD5"));
        System.out.println(getHash(sk, "MD5"));
        System.out.println(getHash(sk, "sha-256").equals(getHash(sk, "sha-256")));
        System.out.println(getHash(sk, "sha-256"));
        System.out.println(getHash(sk, "sha-512"));
        System.out.println(getHash(sk, "sha-512"));

    }
}
