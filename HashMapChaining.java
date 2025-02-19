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

class HashMapChaining
{
  int m_size;
  List<Pair>[] bucket;

  HashMapChaining(int size)
  {
    m_size = size;
    bucket = (List<Pair>[]) new ArrayList[m_size];
  }

  int find(int key)
  {
    int bucketId = hash(key);
    if(bucket[bucketId] == null)
    {
      return -1;
    }
    for(Pair p : bucket[bucketId])
    {
      if(p.key == key)
      {
        return p.val;
      }
    }
    return -1;
  }

  void add(int key, int val)
  {
    int bucketId = hash(key);
    if(bucket[bucketId] == null)
    {
      bucket[bucketId] = new ArrayList<Pair>();
    }
    else
    {
      for(Pair p : bucket[bucketId])
      {
        if(p.key == key)
        {
          p.val = val;
          return;
        }
      }
    }
    bucket[bucketId].add(new Pair(key, val));
    return;
  }

  boolean remove(int key)
  {
    int bucketId = hash(key);
    if(bucket[bucketId] == null)
    {
      return false;
    }
    for(int i=0;i<bucket[bucketId].size();i++)
    {
      if(bucket[bucketId].get(i).key == key)
      {
        bucket[bucketId].remove(i);;
        return true;
      }
    }
    return false;
  }

  int hash(int key)
  {
    return key % m_size;
  }

  public static void main(String[] args)
  {
    int size = Integer.parseInt(args[0]);
    HashMapChaining map = new HashMapChaining(size);
    HashMap<Integer, Integer> hmap = new HashMap<>();
    int maxValue = 100000;
    int numOfTrials = Integer.parseInt(args[1]);
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
      System.out.println("Fail: " + "expected: " + checkSum2 + " actual:" + checkSum1);
    }
  }
}
