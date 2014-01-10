package abacus.search.facets;

import java.io.File;
import java.util.List;

import org.apache.lucene.facet.sortedset.SortedSetDocValuesAccumulator;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MultiCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import abacus.search.facets.BytesRefFacetAccumulator;
import abacus.search.facets.FacetAccumulator;
import abacus.search.facets.FacetValue;
import abacus.search.facets.FastDocValuesAtomicReader;
import abacus.search.facets.MultiBytesRefFacetAccumulator;
import abacus.search.facets.NumericFacetAccumulator;

public class TestLargeIndex {
  
  public static IndexReader getIndexReader(Directory dir, boolean useDirect) throws Exception {
    IndexReader reader = DirectoryReader.open(dir);
    if (true) {
      List<AtomicReaderContext> leaves = reader.leaves();
      
      AtomicReader[] subreaders = new AtomicReader[leaves.size()];
      int i = 0;
      for (AtomicReaderContext leaf : leaves) {
        AtomicReader atomicReader = leaf.reader();
        subreaders[i++] = new FastDocValuesAtomicReader(atomicReader, useDirect);
      }
      
      reader = new MultiReader(subreaders, true);
    }
    
    return reader;
  }
  
  public static void main(String[] args) throws Exception {
    File idxDir = new File(args[0]);
    
    Directory dir = FSDirectory.open(idxDir);
    IndexReader reader = getIndexReader(dir, true);
    
    IndexSearcher searcher = new IndexSearcher(reader);
    
    TopScoreDocCollector tdCollector = TopScoreDocCollector.create(20, true);
    
    FacetAccumulator yearFacetCollector = new NumericFacetAccumulator("year");
    
    BytesRefFacetAccumulator colorFacetCollector = new BytesRefFacetAccumulator("color");
    
    BytesRefFacetAccumulator categoryFacetCollector = new BytesRefFacetAccumulator("category");
    
    FacetAccumulator priceFacetCollector = new NumericFacetAccumulator("price");
    
    FacetAccumulator mileageFacetCollector = new NumericFacetAccumulator("mileage");
    
    FacetAccumulator catchAllFacetCollector = new MultiBytesRefFacetAccumulator("catchall");
    
    //QueryParser qp = new QueryParser(Version.LUCENE_44, "contents", new StandardAnalyzer(Version.LUCENE_44));
    //Query q = qp.parse("tags_indexed:macho");
    Query q = new MatchAllDocsQuery();
    
    Collector collector = MultiCollector.wrap(
        tdCollector,
        //yearFacetCollector, 
        //colorFacetCollector,
        //categoryFacetCollector,
        //priceFacetCollector,
        //mileageFacetCollector
        catchAllFacetCollector
    );
    
    long start = System.currentTimeMillis();
    searcher.search(q, 
        //new EarlyTerminationCollector(1000, collector)
        collector
    );
    
    long send = System.currentTimeMillis();
    TopDocs td = tdCollector.topDocs();
    
    
   
    //FacetValue[] yearValues = yearFacetCollector.getTopFacets(10, 1);
    //FacetValue[] colorValues = colorFacetCollector.getTopFacets(10, 1);
    //FacetValue[] categoryValues = categoryFacetCollector.getTopFacets(10, 1);
    //FacetValue[] priceValues = priceFacetCollector.getTopFacets(10, 1);
    //FacetValue[] milageValues = mileageFacetCollector.getTopFacets(10, 1);
    FacetValue[] catchAllValues = catchAllFacetCollector.getTopFacets(10, 1);
    long end = System.currentTimeMillis();
    
    System.out.println("search/collect: " + (send - start) + ", total docs: " + td.totalHits);
    System.out.println("took: " + (end - start));
    /*
    System.out.println("count : " + colorValues.length);
    System.out.println("count : " + yearValues.length);
    System.out.println("count : " + categoryValues.length);
    System.out.println("count : " + priceValues.length);
    System.out.println("count : " + milageValues.length);
    */

    System.out.println("count : " + catchAllValues.length);
    reader.close();
    
  }
}