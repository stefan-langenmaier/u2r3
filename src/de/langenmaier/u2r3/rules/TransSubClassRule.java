package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.DeltaRelation;
import de.langenmaier.u2r3.Reason;
import de.langenmaier.u2r3.ReasonProcessor;
import de.langenmaier.u2r3.db.SubClassRelation;

public class TransSubClassRule extends Rule {
	static Logger logger = Logger.getLogger(TransSubClassRule.class);
	
	@Override
	public void apply(DeltaRelation delta) {
		ReasonProcessor rp = ReasonProcessor.getReasonProcessor();
		
		if (delta == null) {
			logger.debug("TransSubClassRule");
			if (Math.random()*100 > 50 ) {
				rp.add(new Reason(SubClassRelation.getRelation()));
			}
		}
		
	}

}
