import java.io.*;
import java.util.*;

class Pair
{
  int key;
  int val;
  Pair(int key, int val)
  {
    this.key = key;
    this.val = val;
  }
}

class Bucket
{
  int localDepth;
  Pair[] data;
  Bucket(int localDepth, int bucketSize)
  {
    this.localDepth = localDepth;
    data = new Pair[bucketSize];
  }
}

class ExtendibleHashMap
{
  int globalDepth;
  int bucketSize;
  Bucket[] directory;

  ExtendibleHashMap(int globalDepth, int bucketSize)
  {
    this.globalDepth = globalDepth;
    this.bucketSize = bucketSize;
    directory = new Bucket[1 << globalDepth];
    for(int i=0;i<directory.length;i++)
    {
      directory[i] = new Bucket(globalDepth, bucketSize);
    }
  }

  void doubleDirectorySize()
  {
    globalDepth++;
    Bucket[] newDirectory = new Bucket[1 << globalDepth];
    int oldL = directory.length;
    for(int i=0;i<oldL;i++)
    {
      newDirectory[i] = directory[i];
    }
    for(int i=0;i<oldL;i++)
    {
      newDirectory[i+oldL] = directory[i];
    }
    directory = newDirectory;
  }

  int find(int key)
  {
    int bucketId = getBucketId(key); 
    for(int i=0;i<bucketSize;i++)
    {
      if (directory[bucketId].data[i] != null && directory[bucketId].data[i].key == key)
      {
        return directory[bucketId].data[i].val;
      }
    }
    return -1;
  }

  void add(int key, int val)
  {
    // find the bucket using binary representation of hashed value and masking it to get K least significant bits
    int bucketId = getBucketId(key); 
    int index = 0;
    while(index < bucketSize && directory[bucketId].data[index] != null && directory[bucketId].data[index].key != key)
    {
      index++;
    }
    if(index == bucketSize)
    {
      // bucket is full
      int localDepth = directory[bucketId].localDepth;
      if(localDepth == globalDepth)
      {
        doubleDirectorySize();
      }
      // increase the local depth of the current bucket and create an overflow bucket with the same depth
      Bucket currentBucket = directory[bucketId];
      currentBucket.localDepth++;
      Bucket overflowBucket = new Bucket(currentBucket.localDepth, bucketSize);
      
      // rehash keys in the current bucket so they could possibly go into the overflow bucket
      int overflowBucketDataIndex=0;
      for(int i=0;i<bucketSize;i++)
      {
        int newBucketId = getBucketId(currentBucket.data[i].key);
        if ((newBucketId | (1 << localDepth)) == newBucketId)
        {
          overflowBucket.data[overflowBucketDataIndex++] = currentBucket.data[i]; 
          currentBucket.data[i] = null;
        }
      }
      for(int i=0;i<directory.length;i++)
      {
        // iterate through directory and find all the locations where the currentBucket is
        if(directory[i] == currentBucket)
        {
          if ((i | (1 << localDepth)) == i)
          {
            directory[i] = overflowBucket;
          }
        }
      }
      // try inserting the new (K, V) pair now. Hopefully, it fits in an empty slot
      add(key, val);
      return;
    }
    else if(directory[bucketId].data[index] == null)
    {
      // found an empty slot. But the bucket might still contain the key
      for(int i=index+1;i<bucketSize;i++)
      {
        if(directory[bucketId].data[i] != null && directory[bucketId].data[i].key == key)
        {
          // found a duplicate key. Overwrite it.
          directory[bucketId].data[i].val = val;
          return;
        }
      }
      // no duplicate found. Put the (K, V) pair in the first empty slot
      directory[bucketId].data[index] = new Pair(key, val);
    }
    else
    {
      // found a duplicate. Overwrite it.
      directory[bucketId].data[index].val = val;
    }
    return;
  }

  boolean remove(int key)
  {
    int bucketId = getBucketId(key); 
    for(int i=0;i<bucketSize;i++)
    {
      if (directory[bucketId].data[i] != null && directory[bucketId].data[i].key == key)
      {
        directory[bucketId].data[i] = null;
        return true;
      }
    }
    return false;
    // note: underflow not implemented
  }

  int getBucketId(int key)
  {
    return hash(key) & ((1 << globalDepth) -1);
  }

  int getGlobalDepth()
  {
    return globalDepth;
  }

  int getLocalDepth(int bucketId)
  {
    return directory[bucketId].localDepth;
  }

  void printDirectory()
  {
    for(int i=0;i<directory.length;i++)
    {
      System.out.print(i + " " + directory[i]);
      System.out.print(" (" + directory[i].localDepth + ")");
      for(int j=0;j<bucketSize;j++)
      {
        if(directory[i].data[j] != null)
        {
          System.out.print("\t" + directory[i].data[j].key);
        }
        else
        {
          System.out.print("\t" + "null"); 
        }
      }
      System.out.println();
    }
    System.out.println();
  }

  int hash(int key)
  {
    return (int) (31.0*key/17.0); 
  }

  static void test()
  {
    {
      ExtendibleHashMap map = new ExtendibleHashMap(2, 2);
      //double, rehash successful, new key in old bucket
      map.add(0,0);
      map.add(4,4);
      map.add(8,8);
      //map.printDirectory();
    }
    {
      ExtendibleHashMap map = new ExtendibleHashMap(2, 2);
      //double, rehash successful, new key in new bucket
      map.add(0,0);
      map.add(4,4);
      map.add(12,12);
      //map.printDirectory();
    }
    {
      ExtendibleHashMap map = new ExtendibleHashMap(2, 2);
      //double, rehash failed, double, rehash successful, new key in old bucket
      map.add(0,0);
      map.add(8,8);
      map.add(16,16);
      //map.printDirectory();
    }
    {
      ExtendibleHashMap map = new ExtendibleHashMap(2, 2);
      //double, rehash failed, double, rehash successful, new key in new bucket
      map.add(0,0);
      map.add(8,8);
      map.add(24,24);
      //map.printDirectory();
    }
    {
      ExtendibleHashMap map = new ExtendibleHashMap(2, 2);
      //double, rehash failed, double, rehash successful, new key in old bucket
      map.add(0,0);
      map.add(8,8);
      map.add(16,16);
      map.add(4,4);
      map.add(20,20);
      map.add(12,12);
      map.add(52,52);
      //map.printDirectory();
    }
  }

  public static void main(String[] args)
  {
    int initialDirectorySize = Integer.parseInt(args[0]);
    int bucketSize = Integer.parseInt(args[1]);
    test();
    ExtendibleHashMap map = new ExtendibleHashMap(initialDirectorySize, bucketSize);
    HashMap<Integer, Integer> hmap = new HashMap<>();
    int maxValue = 100000;
    int numOfTrials = Integer.parseInt(args[2]);
    Random rand = new Random();
    rand.setSeed(0);
    for(int i=0;i<numOfTrials;i++)
    {
      int key = rand.nextInt(maxValue);
      int val = rand.nextInt(maxValue);
      map.add(key, val);
      hmap.put(key, val);
      int delKey = rand.nextInt(maxValue);
      map.remove(delKey);
      hmap.remove(delKey);

    }
    int checkSum1 = 0;
    int checkSum2 = 0;
    for(int i=0;i<numOfTrials;i++)
    {
      int key = rand.nextInt(maxValue);
      checkSum1 += map.find(key);
      Integer val = hmap.get(key);
      if(val == null)
      {
        checkSum2 = checkSum2 -1;
      }
      else
      {
        checkSum2 += val;
      }
    }
    if(checkSum1 == checkSum2)
    {
      System.out.println(checkSum1 + " Pass");
    }
    else
    {
      System.out.println("Fail: " + "expected: " + checkSum2 + " actual:" + checkSum1 + " globalDepth: " + map.getGlobalDepth());
      //map.printDirectory();
    }
  }
}
