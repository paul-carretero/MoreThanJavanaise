# MoreThanJavanaise

<h2>Fonctionnalités Client (serveur)</h2>
<ul>
   <li>Javanaise V1 (service d'objet avec cache)</li>
   <li>Support multithreading serveur client</li>
   <li>Cache local de capacité finie avec politique d'éviction de type LRU</li>
   <li>Support des transactions sur plusieurs objets (1 transaction par thread gérée)</li>
   <li>Levée d'exception lorsqu'une tentative d'upgrade de verrou echoue (Concurrent lock upgrade)</li>
   <li>tentative de reconnection en cas d'echec de connexion avec le coordinateur</li>
   <li>proxy (Javanaise V2)</li>
   <li>Prise en compte des références vers d'autre objets javanaise dans un objet javanaise</li>
</ul>


<h2>Fonctionnalités Coordinateurs</h2>
<ul>
   <li>Javanaise V1 (standard)</li>
   <li>Redondance master/slave</li>
   <li>Synchro avec slave asynchrone (pas de perte de latence avec le client)</li>
   <li>Multithreadé et accès concurrents (par objet unique)</li>
</ul>


<h2>Fonctionnalités LoadBalancer</h2>
<ul>
   <li>Répartition de charge entre plusieurs coordinateurs (à l'initialisation)</li>
   <li>Redance master/slave (upgrade auto du slave)</li>
   <li>Auto organisation (dynamique) des coordinateurs en fonction des machines physiques disponibles</li>
   <li>Lancement des coordinateurs</li>
   <li>Upgrade des coordinateurs slave en master (auto)</li>
</ul>


<h2>Tests</h2>
<ul>
   <li>Junits : test des objets, des transactions, du serveur local</li>
   <li>Test de montée en charge</li>
   <li>Test de redondance</li>
</ul>
