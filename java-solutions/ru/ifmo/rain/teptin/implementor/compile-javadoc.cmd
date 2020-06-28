SET root_path=%cd%\..\..\..\..\..\..\..\java-advanced-2020
SET modules=%root_path%\..\java-advanced-2020-solutions\java-solutions
SET korneev_impl_dir=%root_path%\modules\info.kgeorgiy.java.advanced.implementor\info\kgeorgiy\java\advanced\implementor

SET out=%cd%\_build
SET from=%cd%

cd ..\..\..\..\..\..\..

javadoc -d %from%\_javadoc -link https://docs.oracle.com/en/java/javase/13/docs/api^
 -private -author %modules%/ru/ifmo/rain/teptin/implementor/*.java^
 %korneev_impl_dir%\Impler.java^
 %korneev_impl_dir%\JarImpler.java^
 %korneev_impl_dir%\ImplerException.java

cd %from%