package org.mvss.karta.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.runtime.KartaRuntime;
import org.mvss.karta.framework.runtime.RunTarget;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class KartaMain
{
   private static final String  KARTA         = "Karta";

   private static final String  HELP          = "help";

   private static final String  TAGS          = "tags";

   private static final String  FEATURE_FILE  = "featureFile";

   private static final String  JAVA_TEST     = "javaTest";
   private static final String  JAVA_TEST_JAR = "javaTestJar";

   public static List<Runnable> exitHooks     = Collections.synchronizedList( new ArrayList<Runnable>() );

   private static void jvmExitHook()
   {
      log.info( "******************** Stopping Karta  *********************" );
      List<Runnable> exitHooks = new ArrayList<Runnable>( KartaMain.exitHooks );

      log.info( "Triggering registered exit hooks" );
      for ( Runnable exitHook : exitHooks )
      {
         new Thread( exitHook ).run();
      }
   }

   public static void main( String[] args )
   {
      Options options = new Options();
      HelpFormatter formatter = new HelpFormatter();
      DefaultParser parser = new DefaultParser();

      options.addOption( "t", TAGS, true, "tags to run" );

      options.addOption( "f", FEATURE_FILE, true, "feature file to run" );

      options.addOption( "j", JAVA_TEST, true, "test case class to run" );
      options.addOption( JAVA_TEST_JAR, true, "jar file which contains the test" );

      options.addOption( null, HELP, false, "prints this help message" );

      try
      {
         CommandLine cmd = parser.parse( options, args );

         if ( cmd.hasOption( HELP ) )
         {
            formatter.printHelp( KARTA, options );
            System.exit( 0 );
         }
         else
         {

            boolean optionMissing = true;
            boolean runTargetAvailable = false;

            RunTarget runTarget = new RunTarget();

            if ( cmd.hasOption( JAVA_TEST ) )
            {
               optionMissing = false;
               runTarget.setJavaTest( cmd.getOptionValue( JAVA_TEST ) );
            }

            if ( cmd.hasOption( JAVA_TEST_JAR ) )
            {
               optionMissing = false;
               runTarget.setJavaTestJarFile( cmd.getOptionValue( JAVA_TEST_JAR ) );
            }

            if ( cmd.hasOption( FEATURE_FILE ) )
            {
               optionMissing = false;
               runTarget.setFeatureFile( cmd.getOptionValue( FEATURE_FILE ) );
            }

            if ( cmd.hasOption( TAGS ) )
            {
               optionMissing = false;
               HashSet<String> tags = new HashSet<String>();
               for ( String tag : cmd.getOptionValue( TAGS ).split( "," ) )
               {
                  tags.add( tag );
               }
               runTarget.setTags( tags );
            }

            Runtime.getRuntime().addShutdownHook( new Thread( () -> jvmExitHook() ) );

            runTargetAvailable = runTargetAvailable || StringUtils.isNotBlank( runTarget.getFeatureFile() );
            runTargetAvailable = runTargetAvailable || StringUtils.isNotBlank( runTarget.getJavaTest() );
            runTargetAvailable = runTargetAvailable || ( runTarget.getTags() != null && !runTarget.getTags().isEmpty() );

            if ( runTargetAvailable )
            {

               KartaRuntime kartaRuntime = KartaRuntime.getInstance();
               if ( !kartaRuntime.runTestTarget( runTarget ) )
               {
                  System.exit( 1 );
               }
            }
            else
            {
               if ( optionMissing )
               {
                  formatter.printHelp( KARTA, options );
                  System.exit( -1 );
               }
            }
         }
      }
      catch ( UnrecognizedOptionException | MissingArgumentException uoe )
      {
         System.err.println( uoe.getMessage() );
         formatter.printHelp( KARTA, options );
         System.exit( -1 );
      }
      catch ( Throwable t )
      {
         log.error( "Exception caught while init", t );
         formatter.printHelp( KARTA, options );
         System.exit( -1 );
      }
   }
}
