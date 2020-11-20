package org.mvss.karta.framework.runtime.event;

import org.mvss.karta.framework.core.TestScenario;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
public class ScenarioStartEvent extends ScenarioEvent
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public ScenarioStartEvent( String runName, String featureName, long iterationNumber, TestScenario scenario )
   {
      super( StandardEventsTypes.SCENARIO_START_EVENT, runName, featureName, iterationNumber, scenario );
   }
}
