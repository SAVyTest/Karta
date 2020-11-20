package org.mvss.karta.framework.runtime;

public class Constants
{
   public static final String   KARTA                            = "Karta";
   public static final String   __KARTA__                        = "__Karta__";

   public static final String   KARTA_BASE_PLUGIN_CONFIG_YAML    = "KartaBasePluginsConfiguration.yaml";
   public static final String   KARTA_PLUGINS_CONFIG_YAML        = "KartaPluginsConfiguration.yaml";
   public static final String   KARTA_CONFIGURATION_YAML         = "KartaConfiguration.yaml";
   public static final String   KARTA_RUNTIME_PROPERTIES_YAML    = "KartaRuntimeProperties.yaml";

   public static final String   TEST_CATALOG_FILE_NAME           = "KartaTestCatalog.yaml";
   public static final String   TEST_CATALOG_FRAGMENT_FILE_NAME  = "KartaTestCatalogFragment.yaml";

   public static final String   KARTA_RUNTIME                    = "kartaRuntime";
   public static final String   STEP_RUNNER                      = "stepRunner";
   public static final String   TEST_DATA_SOURCES                = "testDataSources";
   public static final String   TEST_JOB                         = "testJob";
   public static final String   ITERATION_COUNTER                = "iterationCounter";

   public static final String   STEP_RUNNER_PLUGIN               = "stepRunnerPlugin";
   public static final String   TEST_DATA_SOURCE_PLUGINS         = "testDataSourcePlugins";

   public static final String   NULL_STRING                      = (String) null;
   public static final String   EMPTY_STRING                     = "";

   public static final String   DOT                              = ".";
   public static final String   SLASH                            = "/";
   public static final String   COLON                            = ":";
   public static final String   HYPHEN                           = "-";

   public static final String   JAR                              = "jar";

   public static final String   JSON                             = "json";
   public static final String   YAML                             = "yaml";
   public static final String   YML                              = "yml";
   public static final String   XML                              = "xml";
   public static final String   PROPERTIES                       = "properties";

   public static final String[] jarExtention                     = {JAR};
   public static final String[] dataFileExtentions               = {JSON, YAML, YML, XML};

   public static final String   UNNAMED                          = "Unnamed";

   public static final String   __GENERIC_FEATURE__              = "__generic_feature__";
   public static final String   __FEATURE_SETUP__                = "__feature_setup__";
   public static final String   __GENERIC_SCENARIO__             = "__generic_scenario__";
   public static final String   __GENERIC_STEP__                 = "__generic_step__";
   public static final String   __FEATURE_TEARDOWN__             = "__feature_teardown__";

   public static final String   _SETUP_                          = ":Setup:";
   public static final String   _TEARDOWN_                       = ":TearDown:";

   public static final String   __TESTS__                        = "__tests__";

   public static final String   CONTENT_TYPE                     = "Content-Type";
   public static final String   APPLICATION_JSON                 = "application/json";
   public static final String   ACCEPT                           = "Accept";

   public static final String   PATH_HEALTH                      = "/health";
   public static final String   PATH_RUN_STEP                    = "/run/step";
   public static final String   PATH_RUN_CHAOS_ACTION            = "/run/choasAction";
   public static final String   PATH_RUN_SCENARIO                = "/run/scenario";
   public static final String   PATH_RUN_JOB                     = "/run/job";
   public static final String   PATH_RUN_JOB_ID                  = "/run/job/{id}";
   public static final String   PATH_RUN_FEATURE                 = "/run/feature";
   public static final String   PATH_RUN_FEATURESOURCE           = "/run/featureSource";
   public static final String   PATH_RUN_TARGET                  = "/run/target";

   public static final String   PATH_API_DEF                     = "/api";

   public static final String   RUN_NAME                         = "runName";
   public static final String   FEATURE                          = "feature";
   public static final String   FEATURE_NAME                     = "featureName";
   public static final String   JOB                              = "job";
   public static final String   ITERATION_NUMBER                 = "iterationNumber";
   public static final String   ITERATION_INDEX                  = "iterationIndex";
   public static final String   SCENARIO                         = "scenario";
   public static final String   CHAOS_ACTION                     = "chaosAction";
   public static final String   SCENARIO_NAME                    = "scenarioName";
   public static final String   STEP                             = "step";
   public static final String   RESULT                           = "result";
   public static final String   STEP_IDENTIFIER                  = "stepIdentifier";
   public static final String   INCIDENT                         = "incident";

   public static final String   TEST_STEP                        = "testStep";
   public static final String   TEST_EXECUTION_CONTEXT           = "testExecutionContext";
   public static final String   SCENARIO_SETUP_STEPS             = "scenarioSetupSteps";
   public static final String   TEST_SCENARIO                    = "testScenario";
   public static final String   SCENARIO_TEAR_DOWN_STEPS         = "scenarioTearDownSteps";
   public static final String   SCENARIO_ITERATION_NUMBER        = "scenarioIterationNumber";

   public static final String   CHANCE_BASED_SCENARIO_EXECUTION  = "chanceBasedScenarioExecution";
   public static final String   EXCLUSIVE_SCENARIO_PER_ITERATION = "exclusiveScenarioPerIteration";
   public static final String   NUMBER_OF_ITERATIONS             = "numberOfIterations";
   public static final String   NUMBER_OF_ITERATIONS_IN_PARALLEL = "numberOfIterationsInParallel";

}
