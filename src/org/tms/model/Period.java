package org.tms.model;

import java.util.stream.Stream;

public enum Period {
	ALL("All"),
	LAST_7_DAYS("Last 7 days"),
	LAST_30_DAYS("Last 30 days");

	private final String period;

	Period(String period) {
		this.period = period;
	}

	public String getPeriod() {
		return period;
	}

	public static Stream<Period> stream() {
		return Stream.of(Period.values()); 
	}

	public static Period fromValue(String period) {
		for (Period periodId : Period.values()) {
			if (periodId.getPeriod() == period) {
				return periodId;
			}
		}
		return Period.LAST_7_DAYS;
	}

}
