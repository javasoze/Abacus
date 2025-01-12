package abacus.indexing;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.SortedSetDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.FacetLabel;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.util.BytesRef;

public class AbacusIndexer {

  private static final FieldType INDEXED_TERM_TYPE;
  
  static {
    INDEXED_TERM_TYPE = new FieldType();
    INDEXED_TERM_TYPE.setStored(false);   
    INDEXED_TERM_TYPE.setIndexOptions(IndexOptions.DOCS);
    INDEXED_TERM_TYPE.setOmitNorms(true);
    INDEXED_TERM_TYPE.setStoreTermVectors(false);
    INDEXED_TERM_TYPE.setTokenized(false);
    INDEXED_TERM_TYPE.freeze();           
  }

  public static final String srcDataFieldName = "_srcdata";

  public static Document addSrcDataField(Document doc, String val) {
    StringField field = new StringField(srcDataFieldName, val, Store.YES);
    doc.add(field);
    return doc;
  }

  public static Document addAttributeField(Document doc, String fieldName, String name, String val) {
    FacetLabel cp = new FacetLabel(name, val);
    String fullPath = FacetsConfig.pathToString(cp.components, cp.length);

    // For facet counts
    Field docValField = new SortedSetDocValuesField(fieldName, new BytesRef(fullPath));
    doc.add(docValField);
    return doc;
  }
  
  public static Document addFacetTermField(Document doc, String fieldName, String val, boolean multi) {
    Field indexedPart = new Field(fieldName, val, INDEXED_TERM_TYPE);
    Field docValPart;
    if (multi) {
      docValPart = new SortedSetDocValuesField(fieldName, new BytesRef(val));
    } else {
      docValPart = new SortedDocValuesField(fieldName, new BytesRef(val));
    }
    doc.add(indexedPart);
    doc.add(docValPart);
    return doc;
  }
  
  public static Document addNumericField(Document doc, String fieldName, int value) {
    Field indexedPart = new IntField(fieldName, value, Store.NO);
    doc.add(indexedPart);
    Field docValPart = new NumericDocValuesField(fieldName, value);    
    doc.add(docValPart);
    return doc;
  }  
  
  public static Document addNumericField(Document doc, String fieldName, float value) {
    Field indexedPart = new FloatField(fieldName, value, Store.NO);
    doc.add(indexedPart);    
    int intVal = Float.floatToRawIntBits(value);
    Field docValPart = new NumericDocValuesField(fieldName, intVal);    
    doc.add(docValPart);
    return doc;
  }  
  
  public static Document addNumericField(Document doc, String fieldName, long value) {
    Field indexedPart = new LongField(fieldName, value, Store.NO);
    doc.add(indexedPart);
    Field docValPart = new NumericDocValuesField(fieldName, value);    
    doc.add(docValPart);
    return doc;
  }  
  
  public static Document addNumericField(Document doc, String fieldName, double value) {
    Field indexedPart = new DoubleField(fieldName, value, Store.NO);
    long longVal = Double.doubleToRawLongBits(value);
    doc.add(indexedPart);
    Field docValPart = new NumericDocValuesField(fieldName, longVal);    
    doc.add(docValPart);
    return doc;
  }
}
