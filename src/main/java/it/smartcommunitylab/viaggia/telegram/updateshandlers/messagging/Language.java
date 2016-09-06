package it.smartcommunitylab.viaggia.telegram.updateshandlers.messagging;

import java.util.Locale;


public enum Language  {
	
	ITALIANO	(new Locale("it", "IT")),
    ENGLISH		(new Locale("en", "US")),
    ESPANOL		(new Locale("es", "ES"));
    
    private Locale locale;
	
	Language(Locale locale) {
		this.locale = locale;
	}
	
	public Locale locale() {
		return locale;
	}
}
