package org.mvss.karta.framework.runtime.event;

import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.runtime.Constants;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
public class FeatureSetupStepStartEvent extends FeatureEvent
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public FeatureSetupStepStartEvent( String runName, TestFeature feature, TestStep step )
   {
      super( StandardEventsTypes.FEATURE_SETUP_STEP_START_EVENT, runName, feature );
      this.parameters.put( Constants.STEP, step );
   }

   @JsonIgnore
   public TestStep getStep()
   {
      return (TestStep) parameters.get( Constants.STEP );
   }
}
