package org.mvss.karta.framework.runtime.event;

import org.mvss.karta.framework.core.StepResult;
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
public class JavaFeatureTearDownCompleteEvent extends FeatureEvent
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public JavaFeatureTearDownCompleteEvent( String runName, String featureName, String stepIdentifier, StepResult result )
   {
      super( StandardEventsTypes.JAVA_FEATURE_TEARDOWN_COMPLETE_EVENT, runName, featureName );
      this.parameters.put( Constants.STEP_IDENTIFIER, stepIdentifier );
      this.parameters.put( Constants.RESULT, result );
   }

   @JsonIgnore
   public String getStepIdentifier()
   {
      return parameters.get( Constants.STEP_IDENTIFIER ).toString();
   }

   @JsonIgnore
   public StepResult getResult()
   {
      return (StepResult) parameters.get( Constants.RESULT );
   }
}
