package org.mvss.karta.framework.runtime.event;

import org.mvss.karta.framework.core.TestJob;
import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.utils.DataUtils;
import org.mvss.karta.framework.utils.ParserUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
@NoArgsConstructor
public abstract class JobEvent extends FeatureEvent
{
   private static final long serialVersionUID = 1L;

   public JobEvent( Event event )
   {
      super( event );
      parameters.put( Constants.JOB, ParserUtils.convertValue( DataFormat.JSON, parameters.get( Constants.JOB ), TestJob.class ) );
      parameters.put( Constants.ITERATION_NUMBER, ParserUtils.convertValue( DataFormat.JSON, parameters.get( Constants.ITERATION_NUMBER ), Long.class ) );
   }

   public JobEvent( String eventType, String runName, String featureName, TestJob job, long iterationNumber )
   {
      super( eventType, runName, featureName );
      this.parameters.put( Constants.JOB, job );
      this.parameters.put( Constants.ITERATION_NUMBER, iterationNumber );
   }

   @JsonIgnore
   public TestJob getJob()
   {
      return (TestJob) parameters.get( Constants.JOB );
   }

   @JsonIgnore
   public long getIterationNumber()
   {
      return DataUtils.serializableToLong( parameters.get( Constants.ITERATION_NUMBER ), -1 );
   }
}
