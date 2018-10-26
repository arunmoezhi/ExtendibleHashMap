import java.io.*;
import java.util.*;
class Parameters
{
  static final int numOfRecords = 2;
}

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
  Bucket(int localDepth)
  {
    this.localDepth = localDepth;
    data = new Pair[Parameters.numOfRecords];
  }
}

class ExtendibleHashMap
{
  int globalDepth;
  Bucket[] directory;
  
  ExtendibleHashMap(int globalDepth)
  {
    this.globalDepth = globalDepth;
    directory = new Bucket[1 << globalDepth];
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
    int bucketId = hash(key) & ((1 << globalDepth) -1);
    if(directory[bucketId] == null)
    {
      return -1;
    }
    for(int i=0;i<Parameters.numOfRecords;i++)
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
    int bucketId = hash(key) & ((1 << globalDepth) -1);
    if(directory[bucketId] == null)
    {
      // bucket is empty. Create a new bucket and put the (K, V) pair in it
      directory[bucketId] = new Bucket(globalDepth);
      directory[bucketId].data[0] = new Pair(key, val);
    }
    // bucket not empty. Search for the first available empty slot and also look for a duplicate key.
    int index = 0;
    while(index < Parameters.numOfRecords && directory[bucketId].data[index] != null && directory[bucketId].data[index].key != key)
    {
      index++;
    }
    if(index == Parameters.numOfRecords)
    {
      // bucket is full
      if(directory[bucketId].localDepth < globalDepth)
      {
        // no need to double directory size.
      }
      else
      {
        doubleDirectorySize();
        // rehash this bucket entries
      }
    }
    else if(directory[bucketId].data[index] == null)
    {
      // found an empty slot. But the bucket might still contain the key
      for(int i=index+1;i<Parameters.numOfRecords;i++)
      {
        if(directory[bucketId].data[index] != null && directory[bucketId].data[index].key == key)
        {
          // found a duplicate key. Overwrite it.
          directory[bucketId].data[index].val = val;
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

  int getGlobalDepth()
  {
    return globalDepth;
  }

  int hash(int key)
  {
    return (int) (31.0*key/17.0); 
  }

  public static void main(String[] args)
  {
    int size = Integer.parseInt(args[0]);
    ExtendibleHashMap map = new ExtendibleHashMap(size);
    System.out.println("Global depth: " + map.getGlobalDepth());
    //map.add(5,50);
    map.add(7,70);
    //map.add(5,500);
    //map.add(13,130);
    //map.add(12,120);
    map.add(0,0);
    map.add(7,700);
    map.add(9,90);
    map.add(11,110);
    System.out.println("Global depth: " + map.getGlobalDepth());
    for(int i=0;i<20;i++)
    {
      System.out.println(i + " " + Integer.toBinaryString(map.hash(i)) + " " +  map.find(i));
    }
    /*
    HashMap<Integer, Integer> hmap = new HashMap<>();
    int maxValue = 1000000;
    int numOfTrials = 10;
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
      System.out.println("Fail");
    }
    */
  }
}
