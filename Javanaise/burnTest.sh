#!/bin/bash
printf "\033c"
echo ''
echo '     ██╗ █████╗ ██╗   ██╗ █████╗ ███╗   ██╗ █████╗ ██╗███████╗███████╗   '
echo '     ██║██╔══██╗██║   ██║██╔══██╗████╗  ██║██╔══██╗██║██╔════╝██╔════╝   '
echo '     ██║███████║██║   ██║███████║██╔██╗ ██║███████║██║███████╗█████╗     '
echo '██   ██║██╔══██║╚██╗ ██╔╝██╔══██║██║╚██╗██║██╔══██║██║╚════██║██╔══╝     '
echo '╚█████╔╝██║  ██║ ╚████╔╝ ██║  ██║██║ ╚████║██║  ██║██║███████║███████╗   '
echo ' ╚════╝ ╚═╝  ╚═╝  ╚═══╝  ╚═╝  ╚═╝╚═╝  ╚═══╝╚═╝  ╚═╝╚═╝╚══════╝╚══════╝   '
echo '                                                                         '
echo '██████╗ ██╗   ██╗██████╗ ███╗   ██╗    ████████╗███████╗███████╗████████╗'
echo '██╔══██╗██║   ██║██╔══██╗████╗  ██║    ╚══██╔══╝██╔════╝██╔════╝╚══██╔══╝'
echo '██████╔╝██║   ██║██████╔╝██╔██╗ ██║       ██║   █████╗  ███████╗   ██║   '
echo '██╔══██╗██║   ██║██╔══██╗██║╚██╗██║       ██║   ██╔══╝  ╚════██║   ██║   '
echo '██████╔╝╚██████╔╝██║  ██║██║ ╚████║       ██║   ███████╗███████║   ██║   '
echo '╚═════╝  ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═══╝       ╚═╝   ╚══════╝╚══════╝   ╚═╝   '
echo ''
echo '               ██████╗ ███████╗ █████╗ ██████╗ ██╗   ██╗    ██╗'
echo '               ██╔══██╗██╔════╝██╔══██╗██╔══██╗╚██╗ ██╔╝    ██║'
echo '               ██████╔╝█████╗  ███████║██║  ██║ ╚████╔╝     ██║'
echo '               ██╔══██╗██╔══╝  ██╔══██║██║  ██║  ╚██╔╝      ╚═╝'
echo '               ██║  ██║███████╗██║  ██║██████╔╝   ██║       ██╗'
echo '               ╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝╚═════╝    ╚═╝       ╚═╝'
echo ''
echo '█████████████████████████████████████████████████████████████████████████'

cd build

if [ "`ps aux | grep '[r]miregistry'`" = "" ]
then
    echo " -> Lancement de RMIRegistry"
    eval "rmiregistry&"
fi

echo " -> Nombre de Worker ?"
read nbWorker
echo " -> Nombre d'itération ?"
read nbIteration

echo " -> Lancement du Coordinateur"
`gnome-terminal -x sh -c "java -jar main.jar coord ; bash"`

echo " -> Lancement du Manager"
`gnome-terminal -x sh -c "java -jar main.jar manager $nbWorker; bash"`

echo " -> Lancement des Tester"
for i in `seq 1 $nbWorker`;
do
	`gnome-terminal -x sh -c "java -jar main.jar tester $i $nbWorker $nbIteration; bash"`
done
