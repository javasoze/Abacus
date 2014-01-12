package abacus.search.facets;

import java.nio.ByteBuffer;

import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.util.BytesRef;

public class DirectBufferSortedDocValues extends SortedDocValues {

  private final ByteBuffer ords;
  private final ByteBuffer buffer;  
  private final ByteBuffer byteRefs;
  private final int numTerms;
  
  public DirectBufferSortedDocValues(SortedDocValues inner, int maxDoc) {    
    this.ords = ByteBuffer.allocateDirect(maxDoc * 4);
    for (int i =0;i< maxDoc; ++i) {      
      this.ords.putInt(inner.getOrd(i));
    }
    
    numTerms = inner.getValueCount();
    this.byteRefs = ByteBuffer.allocateDirect(numTerms * 8);
    BytesRef[] byteRefArr = new BytesRef[numTerms];
    
    int numBytes = 0;
    for (int i = 0; i < numTerms ; ++i) {
      BytesRef tempRef = new BytesRef();
      inner.lookupOrd(i, tempRef);
      numBytes += tempRef.length;
      byteRefArr[i] = tempRef;
      this.byteRefs.putInt(tempRef.offset);
      this.byteRefs.putInt(tempRef.length);
    }
    buffer = ByteBuffer.allocateDirect(numBytes);    
    for (int i = 0;i < numTerms ;++i) {      
      buffer.put(byteRefArr[i].bytes, byteRefArr[i].offset, byteRefArr[i].length);
    }
  }
  
  @Override
  public int getOrd(int docID) {
    return this.ords.getInt(docID * 4);
  }

  @Override
  public void lookupOrd(int ord, BytesRef result) {
    int offset = byteRefs.getInt(8 * ord );
    int length = byteRefs.getInt(8 * ord + 4);
    
    result.bytes = new byte[length];
    result.offset = 0;
    result.length = length;
    for (int i = 0; i < result.length; ++i) {
      result.bytes[i] = buffer.get(offset + i);
    }
  }

  @Override
  public int getValueCount() {
    return numTerms;
  }

}
