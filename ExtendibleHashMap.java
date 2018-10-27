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
    if(directory[bucketId] == null)
    {
      return -1;
    }
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
    assert(directory[bucketId] != null);
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
      createNewBucket(bucketId); 
      rehashBuckets(bucketId);
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
    return false;
  }
  
  void createNewBucket(int bucketId)
  {
    // create a new bucket in the mirror position of bucketId
    int oldBucketId;
    int newBucketId;
    int localDepth = directory[bucketId].localDepth;
    if((bucketId & (1 << localDepth)) == 0)
    {
      oldBucketId = bucketId;
      newBucketId = bucketId | (1 << localDepth); // 010 OR 100 => 110
    }
    else
    {
      oldBucketId = bucketId & ((1 << localDepth)-1); // 110 AND 011 => 010
      newBucketId = bucketId;
    }
    int newDepth = localDepth+1;
    directory[newBucketId] = new Bucket(newDepth, bucketSize);
    directory[oldBucketId].localDepth = newDepth;
    return;
  }

  void rehashBuckets(int bucketId)
  {
    // rehash this bucket entries
    int localDepth = directory[bucketId].localDepth;
    if((bucketId & (1 << localDepth)) != 0)
    {
      bucketId = bucketId & ((1 << localDepth)-1); // 110 AND 011 => 010
    }
    int newBucketIndex=0;
    for(int i=0;i<bucketSize;i++)
    {
      Pair p = directory[bucketId].data[i];
      if(p == null)
      {
        return;
      }
      int newBucketId = getBucketId(p.key); 
      if(newBucketId == bucketId)
      {
        // cannot move to new bucket 
        continue;
      }
      else
      {
        // move this (K, V) pair from old bucket to new bucket
        directory[newBucketId].data[newBucketIndex++] = directory[bucketId].data[i];
        directory[bucketId].data[i] = null;
      }
    }
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
    assert(bucketId < directory.length);
    return directory[bucketId].localDepth;
  }

  void printDirectory()
  {
    for(int i=0;i<directory.length;i++)
    {
      System.out.print(directory[i]);
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

  public static void main(String[] args)
  {
    int initialDirectorySize = Integer.parseInt(args[0]);
    int bucketSize = Integer.parseInt(args[1]);
    ExtendibleHashMap map = new ExtendibleHashMap(initialDirectorySize, bucketSize);
    /*
    {
      map.add(2,20);
      map.add(4,40);
      map.add(0,0);
      map.add(7,70);
      map.add(9,90);
      map.add(13,130);
    }
    
    {
      // test repeated doubling
      map.add(0,0);
      map.add(9,90);
      map.add(18,180);
    }
    map.printDirectory();
    */
    
    HashMap<Integer, Integer> hmap = new HashMap<>();
    int maxValue = 100;
    int numOfTrials = Integer.parseInt(args[2]);
    Random rand = new Random();
    rand.setSeed(0);
    for(int i=0;i<numOfTrials;i++)
    {
      int key = rand.nextInt(maxValue);
      int val = rand.nextInt(maxValue);
      map.add(key, val);
      hmap.put(key, val);
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
      System.out.println("Fail: " + "expected: " + checkSum2 + " actual:" + checkSum1);
    }
  }
}
