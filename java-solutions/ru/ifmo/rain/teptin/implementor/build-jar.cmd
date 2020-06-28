@echo off

SET package_dir=ru\ifmo\rain\teptin\implementor
SET korneev_dir=info\kgeorgiy\java\advanced\implementor
SET package_name=ru.ifmo.rain.teptin.implementor

SET kgeorgiy=..\..\..\..\..\..\..
SET root_=%cd%\%kgeorgiy%\java-advanced-2020\modules\info.kgeorgiy.java.advanced.implementor\info\kgeorgiy\java\advanced\implementor
SET src=%cd%\..\..\..\..\..
SET out=%cd%\_build
SET from=%cd%

cd %kgeorgiy%
javac %src%\%package_dir%\*.java %root_%\Impler.java %root_%\JarImpler.java %root_%\ImplerException.java -d %out%
cd %out%
jar -c --file=%from%\_implementor.jar --main-class=%package_name%.JarImplementor %package_dir%\*.class %korneev_dir%\*.class
cd ..