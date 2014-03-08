package mo.umac.crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ResultSetForADim {

	private Set<Integer> poiIDs = new HashSet<Integer>();

	private HashMap<Integer, Double> crawledPointsInterval = new HashMap<Integer, Double>();

	public Set<Integer> getPoiIDs() {
		return poiIDs;
	}

	public void setPoiIDs(Set<Integer> poiIDs) {
		this.poiIDs = poiIDs;
	}

	public HashMap<Integer, Double> getCrawledPointsInterval() {
		return crawledPointsInterval;
	}

	public void setCrawledPointsInterval(
			HashMap<Integer, Double> crawledPointsInterval) {
		this.crawledPointsInterval = crawledPointsInterval;
	}

	public void putAll(ResultSetForADim rs) {
		poiIDs.addAll(rs.getPoiIDs());
		crawledPointsInterval.putAll(rs.getCrawledPointsInterval());
	}

	public void addPoiIDs(List<Integer> answer) {
		this.poiIDs.addAll(answer);
	}
	
	public void addPoiIDs(Set<Integer> poiIDs) {
		this.poiIDs.addAll(poiIDs);
	}

	public void addCrawledPointsInterval(
			HashMap<Integer, Double> crawledPointsInterval) {
		this.crawledPointsInterval.putAll(crawledPointsInterval);
	}
	
}
