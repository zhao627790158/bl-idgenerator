package cn.blueshit.idgenerator.util.hash;

import java.security.MessageDigest;

public interface Hashing {

  public static final Hashing MURMUR_HASH = new MurmurHash();
  public ThreadLocal<MessageDigest> md5Holder = new ThreadLocal<MessageDigest>();

  public long hash(String key);

  public long hash(byte[] key);
}