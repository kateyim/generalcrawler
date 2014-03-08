package mo.umac.crawler;

public class ContextDn {

	private Strategy strategy;

	public ContextDn(Strategy strategy) {
		this.strategy = strategy;
	}

	public void callCrawling() {
		this.strategy.callCrawling();
	}

}
