# Spec V1 révisée — Plateforme de portfolios open source

29 oct. 2026 · @cracks

## Vision et positionnement

La plateforme recense des portfolios de développeurs en ligne et dont le code est public : on
peut voir le site et lire comment il est construit. C'est ce qui la distingue d'Awwwards
(concours de design) et de LinkedIn (recrutement).

Nom de travail : **SourceFolio** (provisoire, disponibilité du nom et du domaine à vérifier).
Promesse : *Des portfolios dont tu peux lire le code* / *Portfolios you can read the code of*.

| Pour qui | Valeur reçue | Pourquoi il revient |
| --- | --- | --- |
| Visiteur, étudiant | Inspiration filtrée par pays, spécialité, stack ; code lisible | Nouveaux portfolios chaque semaine |
| Auteur | Visibilité, retours utiles, surveillance gratuite de son site | Stats, retours reçus, bilan de santé |
| Communauté (pays, école) | Vitrine collective partageable | Pages pays et espaces communauté (V1.x) |

Règle de conception : une action principale par écran, le reste en divulgation progressive. Le
produit crée l'habitude par l'utilité (stats, retours, alertes), jamais par des séries, points
ou classements.

## Décisions tranchées

Chaque question ouverte a une réponse par défaut ; elle peut être revue après mesure.

| Question | Décision V1 | Raison |
| --- | --- | --- |
| Portfolio indisponible : visible ou masqué ? | Masqué du flux et de la recherche dès l'état Indisponible temporairement. La fiche reste accessible par lien direct, avec un bandeau. | Un visiteur ne doit jamais tomber sur un lien mort depuis le catalogue. |
| Délais avant retrait | 3 échecs sur 24 h → indisponible ; 14 jours → À corriger ; 21 jours → Retiré. Retour automatique à Publié dès que le site répond à nouveau. | Laisse le temps de régler un DNS ou un hébergeur en panne. |
| Conservation après retrait | 12 mois. Préavis par e-mail et dans l'app 30 jours avant suppression. Le compte n'est jamais supprimé automatiquement. | Restauration possible sans perte des retours. |
| Suppression demandée par l'auteur | Effective sous 30 jours. Ses commentaires sont supprimés, ses appréciations retirées. | Respect des données personnelles. |
| Plusieurs portfolios par développeur ? | Un seul portfolio publié par membre. Le modèle accepte N vitrines par membre pour la suite. | Évite le spam et garde un catalogue lisible. |
| Changement d'URL | Même dépôt source → même portfolio, retours conservés. Nouveau dépôt → nouveau portfolio, l'ancien est archivé avec ses retours. | Règle explicite demandée par la spec initiale. |
| Organisation GitHub comme auteur ? | Non. Dépôts personnels publiés automatiquement ; dépôt d'organisation → vérification manuelle. | Pas de scope read:org à demander. |
| E-mail | Facultatif, demandé à l'étape de publication, prérempli avec l'e-mail public GitHub s'il existe. Texte : « pour être prévenu si ton site tombe ». | Seul moyen fiable d'avertir avant un retrait, sans permission GitHub supplémentaire. |
| Nombre d'appréciations public ? | Caché sur les cartes et la fiche ; visible par l'auteur dans son tableau de bord. | Empêche la popularité de décider de la visibilité. |
| Tri par défaut | Découvrir : mélange aléatoire renouvelé chaque jour, avec 1 carte sur 5 réservée aux portfolios de moins de 14 jours. Tri Récents disponible. Pas de tri Populaires. | Exposition équitable mesurable. |
| Publication automatique | Oui si : dépôt appartenant au membre connecté, champ homepage du dépôt égal à l'URL (ou GitHub Pages du membre), site qui répond. Sinon file de vérification manuelle. | Le manuel ne passe pas à l'échelle pour une petite équipe. |
| Langue par défaut | Langue du navigateur ; à défaut, anglais. Choix mémorisé par compte. | Portée internationale, français natif. |
| Commentaires | Retour guidé : un angle facultatif (Design, Clarté, Performance, Mobile, Code) + texte libre de 10 à 1 000 caractères. | Des retours utiles plutôt que « Nice! ». |

## Navigation et expérience mobile-first

Le mobile est conçu en premier et doit avoir le niveau d'une app native : environ 90 % des
premières visites viendront d'un téléphone, souvent via un lien partagé sur WhatsApp. Le web
desktop reprend la même architecture, adaptée à l'écran large.

### Architecture : 4 onglets

| Onglet | Contenu | Visiteur non connecté |
| --- | --- | --- |
| Découvrir | Flux de cartes, puces de filtres rapides, cloche de notifications en haut à droite | Identique, sans cloche |
| Rechercher | Champ de recherche, filtres complets, recherches récentes | Identique |
| Mon portfolio | Tableau de bord auteur : état, stats, retours, bilan de santé | Écran « Publie ton portfolio » + connexion GitHub |
| Moi | Profil, langue, thème, notifications, déconnexion | Langue, thème, connexion |

Sur mobile : barre d'onglets en bas, dans la zone du pouce. Sur desktop (≥ 1024 px) : barre
latérale gauche, grille de 3 à 4 colonnes, fiche ouverte en panneau latéral sans quitter le
catalogue.

### Écrans clés

- **Carte** : capture du rendu mobile du portfolio sur téléphone, rendu desktop sur ordinateur ;
  nom, spécialité, drapeau du pays, 3 technologies maximum.
- **Filtres** : rangée de puces défilante sous la recherche ; « Filtres » ouvre une feuille en
  bas d'écran avec le compteur en direct (« Voir 124 portfolios »).
- **Fiche** : capture en grand, puis barre d'actions fixée en bas : Voir le site (principal),
  Code, apprécier, commenter, partager.
- **Commentaires** : feuille en bas d'écran, champ de saisie au-dessus du clavier.
- **Site externe** : nouvel onglet (beaucoup de sites interdisent l'affichage dans une iframe) ;
  le clic est compté pour les stats de l'auteur.

### Gestes et comportements

- Position de défilement restaurée au retour d'une fiche : non négociable.
- Retour par balayage, tirer pour rafraîchir, chargement progressif avec squelettes (jamais de
  spinner plein écran).
- Transition partagée carte → fiche (la capture s'agrandit).
- Partage natif du téléphone (Web Share) sur chaque fiche et profil, avec image d'aperçu
  générée pour WhatsApp, X et LinkedIn.
- Cibles tactiles de 44 px minimum, contraste AA, thème clair et sombre selon le système.

### Performance pour les réseaux réels

| Mesure | Objectif |
| --- | --- |
| JavaScript initial | ≤ 150 Ko compressés |
| Affichage du premier contenu utile (LCP) | < 2,5 s en 4G sur Android d'entrée de gamme |
| Images | WebP ou AVIF, tailles adaptées à l'écran, chargement différé |
| Hors connexion | Application installable (PWA), coquille et dernières pages vues en cache |
| Mode économie de données | Captures chargées au toucher si le téléphone signale l'économie de données |

## Parcours clés

Un développeur doit pouvoir publier son portfolio en moins de 60 secondes après la connexion
GitHub, sans formulaire.

### Connexion et publication en 1 clic

1. Le visiteur touche **Publier ton portfolio** ou **Se connecter avec GitHub** (permission :
   profil public uniquement).
2. Retour immédiat au catalogue, connecté. En arrière-plan, la plateforme scanne ses dépôts
   publics :
   - dépôt `pseudo.github.io` ;
   - champ `homepage` des dépôts ;
   - dépôts nommés `portfolio`, `site`, `me`.
3. Si un candidat est trouvé, une carte apparaît : « C'est ton portfolio ? » avec la capture,
   l'URL et le dépôt.
4. **Oui, publier** → vérification automatique (quelques secondes), puis demande des
   informations manquantes : pays et spécialité (préremplis si possible), e-mail facultatif.
5. Publié. Écran de succès avec **Partager** (lien et image d'aperçu prêts pour WhatsApp).
6. Aucun candidat trouvé → saisie manuelle de l'URL du site et du dépôt, même vérification.

### Complétion du profil, une question à la fois

- Plein écran, une question, grande zone tactile, bouton flèche fixé au-dessus du clavier,
  barre de progression, **Passer** sur les questions facultatives, balayage pour revenir.
- Pays : prérempli depuis le champ `location` de GitHub quand il est reconnaissable, à
  confirmer d'un toucher.
- Spécialité : puces à choix unique (Frontend, Backend, Full-stack, Mobile, DevOps, Data/IA,
  Jeu, Embarqué, Sécurité, Autre).
- Technologies : puces avec autocomplétion ; suggestions issues des langages de ses dépôts.
- Présentation : préremplie avec la bio GitHub, 280 caractères maximum.
- Sauvegarde à chaque réponse. Une question déjà répondue n'est jamais reposée. Rappel discret
  dans Mon portfolio tant que pays et spécialité manquent.

### Visiteur venu d'un lien partagé

1. Arrive directement sur une fiche (cas le plus fréquent sur mobile).
2. Voit le portfolio, **Voir le site**, **Code**.
3. En bas de fiche : « D'autres portfolios [Frontend] au [Cameroun] » → entrée naturelle dans
   le catalogue.
4. Apprécier ou commenter déclenche la connexion GitHub, puis l'action est exécutée
   automatiquement au retour.

## Boucles de valeur

L'auteur revient parce que la plateforme l'informe sur son portfolio ; le visiteur revient parce
que le catalogue change. Aucune mécanique de points, séries ou classements.

| Boucle | Déclencheur | Ce que voit le membre | Version |
| --- | --- | --- | --- |
| Stats auteur | Notification hebdomadaire si activité | Vues de la fiche, clics vers le site, clics vers le code, pays des visiteurs, sur 7 et 30 jours | V1 |
| Retours reçus | Notification à chaque commentaire | Le retour, son angle (Design, Clarté…), réponse possible | V1 |
| Bilan de santé | Chaque vérification ; alerte si un point se dégrade | Site en ligne, temps de réponse, liens cassés, image d'aperçu (OG), balises titre et description, rendu mobile | V1 |
| Alerte de panne | 3 échecs consécutifs | Ce qui ne répond pas, délai avant retrait, bouton Corriger le lien | V1 |
| Nouveautés | Visite ou résumé hebdomadaire facultatif par e-mail | Portfolios ajoutés depuis la dernière visite, selon ses filtres habituels | V1 |
| Badge à intégrer | Après publication | Badge « Listé sur SourceFolio » pour README ou site, qui ramène des visiteurs | V1.x |
| Retours réciproques | Demande de retour par l'auteur | File « Ils attendent un retour » ; donner 2 retours donne la priorité à sa propre demande | V1.x |

Le bilan de santé réutilise les vérifications de disponibilité déjà exigées : un coût de
développement faible pour la valeur la plus différenciante. Il reste privé : aucun score n'est
affiché publiquement.

## Cycle de vie du portfolio

Une panne ne retire un portfolio qu'après 21 jours sans correction ; ses retours sont conservés
à chaque étape.

- **Vérifications** : toutes les 24 h pour un portfolio publié. Après un échec, nouvelles
  tentatives à +1 h et +6 h ; au 3e échec consécutif, passage en Indisponible temporairement et
  vérification toutes les 6 h.
- **Notifications** : à l'entrée en Indisponible temporairement, à l'entrée en À corriger
  (« retrait dans 7 jours »), puis au retrait.
- **Domaine expiré ou contenu remplacé** : si l'empreinte du contenu (titre, structure) change
  radicalement ou si la page ressemble à un domaine parqué, passage immédiat en À corriger, car
  le lien peut mener vers un site frauduleux.
- **Retiré** : invisible publiquement, visible dans Mon portfolio ; l'auteur peut corriger le
  lien à tout moment pendant 12 mois.
- Le compte de l'auteur n'est jamais affecté par l'état de son portfolio.

## Modèle de données extensible

Le modèle nomme les choses de façon générique dès la V1 pour accueillir d'autres fournisseurs,
types de vitrines et segments sans migration lourde. Les limites de la V1 sont dans les règles
métier, pas dans le schéma.

| Entité | Champs essentiels | Ce que ça débloque plus tard |
| --- | --- | --- |
| User | id, locale, email (facultatif), created_at | Plusieurs fournisseurs de connexion |
| AuthIdentity | user_id, provider (github), provider_user_id, login | GitLab, Google, designers sans GitHub |
| DeveloperProfile | user_id, display_name, bio + bio_lang, country_code (ISO 3166), specialty_id, champs avec origine (github ou declared) | Distinction données importées / déclarées exigée par la spec |
| Showcase | id, owner_user_id, kind (portfolio), site_url, status, published_at | Projets, side-projects, templates |
| SourceRepo | showcase_id, provider, owner, name, is_public, detected_stack | GitLab, Codeberg |
| ShowcaseStatusEvent | showcase_id, from, to, reason, actor (system, owner, moderator), at | Historique, restauration, traçabilité |
| ShowcaseCheck | showcase_id, at, http_status, response_ms, content_fingerprint, health (JSON), screenshot_mobile, screenshot_desktop | Bilan de santé, détection de domaine expiré |
| Specialty, Technology | slug, aliases, libellés FR/EN | Filtres fiables, React = ReactJS = react.js |
| Reaction | user_id, showcase_id, type (like), unique (user, showcase) | Autres réactions sans nouvelle table |
| Comment | user_id, showcase_id, angle, body, lang, status | Traduction en un clic |
| ShowcaseView | showcase_id, at, event (view, site_click, code_click), country, sans identifiant personnel | Stats auteur |
| Notification | user_id, type, payload, read_at, sent_email_at | Nouveaux canaux |
| ModerationReport | reporter_id, target_type, target_id, reason, status, decision, decided_by | Traçabilité des décisions |
| Community (V1.x) | slug, type (school, country, group), membres N↔N vitrines | Écoles, bootcamps, communautés |

Architecture technique recommandée : un monolithe modulaire, PostgreSQL, une file de tâches avec
trois workers (vérification, capture d'écran, synchronisation GitHub) et un stockage d'objets
pour les captures. Pas de microservices en V1.

## Bilingue FR/EN

Le français et l'anglais sont tous deux complets dès la V0 ; aucune chaîne d'interface n'est
écrite en dur.

| Élément | Règle |
| --- | --- |
| Interface | Fichiers de traduction par clé ; une clé manquante bloque la mise en production |
| URLs | /fr/... et /en/..., balises hreflang, plan du site par langue |
| Choix de langue | Langue du navigateur, puis anglais ; changement dans Moi et en pied de page ; mémorisé sur le compte |
| Données de référence | Codes stockés (CM, frontend, react), libellés traduits |
| Contenu des membres | Jamais traduit automatiquement en V1 ; chaque bio et commentaire stocke sa langue |
| Notifications et e-mails | Modèles dans les deux langues, envoyés dans la langue du destinataire |
| Dates, nombres | Formatés selon la langue (29 oct. 2026 / Oct 29, 2026) |
| Images d'aperçu partagées | Générées dans la langue de l'URL partagée |

## Segments et feuille de route

Chaque étape ajoute un segment sans alourdir l'expérience des précédentes ; on passe à la
suivante quand son critère de passage est atteint.

1. **V0 — Catalogue vivant** : catalogue, recherche, filtres, connexion GitHub, détection et
   publication en 1 clic, vérification et captures, appréciations, bilingue, PWA.
   Passage : 100 portfolios publiés et accessibles.
2. **V1 — Retours et auteurs** : commentaires guidés, stats auteur, bilan de santé,
   notifications et e-mail, cycle de vie complet, signalements et modération.
   Passage : 30 % des auteurs reviennent dans les 30 jours.
3. **V1.x — Communautés** : pages pays (« Les devs du Cameroun »), badge à intégrer, retours
   réciproques, espaces écoles et communautés.
   Passage : une première école ou communauté active.
4. **V2 — Au-delà des portfolios** : vitrines de projets et templates, connexion GitLab et
   Google, traduction en un clic des commentaires.
5. **V3 — Opportunités (sur opt-in)** : mention « ouvert aux opportunités », accès recruteurs ;
   première source de revenus possible, avec les espaces écoles.

## Indicateurs et critères d'acceptation

Indicateur principal : nombre d'auteurs ayant reçu au moins un retour utile dans la semaine.

| Indicateur | Ce qu'il mesure |
| --- | --- |
| Taux de publication en 1 clic après détection | L'onboarding fonctionne |
| Délai médian connexion → publication | Objectif : moins de 60 secondes |
| Retour des auteurs à 30 jours | Stats et bilan de santé créent l'habitude |
| Part des portfolios publiés réellement accessibles | Fiabilité du catalogue (objectif ≥ 98 %) |
| Délai médian panne → résolution | Efficacité des alertes |
| Part des vues allant à des portfolios de moins de 14 jours | Équité de la découverte |
| Répartition par pays et spécialité | Diversité du catalogue |
| Visites arrivant par lien partagé | Croissance par WhatsApp et réseaux |

### Critères d'acceptation ajoutés à la spec initiale

- [ ] Un développeur dont le portfolio est détecté le publie en 1 clic après connexion.
- [ ] Toutes les pages sont disponibles en français et en anglais, avec URL dédiée.
- [ ] Le retour d'une fiche restaure la position de défilement du catalogue.
- [ ] LCP inférieur à 2,5 s en 4G sur un Android d'entrée de gamme.
- [ ] Un portfolio dont le contenu change radicalement passe en À corriger et sort du catalogue.
- [ ] Un portfolio redevenu accessible est republié automatiquement avec ses retours.
- [ ] Le nombre d'appréciations n'est visible que par l'auteur.
- [ ] L'auteur voit ses vues, clics vers le site et clics vers le code.
