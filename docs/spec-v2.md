# Spec V2 — De l'open source à l'opportunité

6 nov. 2026 · @cracks · remplace `spec-v1.md` (catalogue de portfolios)

## Vision

La plateforme encourage les profils tech à contribuer à l'open source et transforme ces
contributions en une **fiche** que les recruteurs peuvent lire et croire.

Promesse : *De l'open source à l'opportunité* / *From open source to opportunity*.
Nom de travail : **SourceFolio** (provisoire).

Ce n'est pas un concours des « meilleurs » : un débutant doit se sentir bienvenu, pas écrasé.
Le public est africain, mais on ne le répète pas partout : la carte le montre.

### Pour qui

| Qui | Ce qu'il vient chercher | En V2 |
| --- | --- | --- |
| Profil tech (développeur, SWE, sysadmin, DevOps, data, mobile, QA, sécurité…) | Se rendre visible par ce qu'il a réellement construit | Gratuit, compte GitHub |
| Recruteur | Trouver des talents par leurs preuves, pas par leurs mots | Liste d'attente (e-mail pro), pas encore de compte |
| Visiteur | Lire une fiche partagée, explorer les classements et la carte | Sans compte |

Le texte à l'écran dit « profil tech » ou « talent », jamais seulement « développeur » : un
sysadmin ou un DevOps doit se reconnaître dans la promesse.

## Décisions tranchées (6 nov. 2026)

| Question | Décision | Raison |
| --- | --- | --- |
| Que devient le catalogue de portfolios ? | Retiré du produit, données comprises : écrans, routes et jobs arrêtés, tables supprimées (`V27`), stockage S3 retiré du déploiement. Le code reste dans l'historique git. | « L'analyse des portfolios » est hors V2. Un seul produit à maintenir. |
| Source des données | GitHub uniquement, données publiques. | Vérifiable par n'importe qui. |
| Accès à l'API GitHub | Un jeton serveur dédié (GitHub App ou jeton fin en lecture publique), en secret sur le serveur. Le scope OAuth de connexion reste `read:user`. | GraphQL exige un jeton ; le rafraîchissement hebdomadaire doit tourner même quand le membre n'est pas connecté. |
| Publication | Rien n'est public sans l'accord explicite du membre. L'analyse donne d'abord un aperçu privé. | Règle non négociable. |
| Pays | Obligatoire pour publier (il détermine les classements). Ville facultative. | Les classements filtrent par pays. |
| Score combiné | Aucun en V2. Chaque classement trie sur un seul indicateur et dit ce qu'il mesure. | Pas assez de vraies données pour pondérer honnêtement. |
| Ordre de livraison | Spec, puis analyse GitHub et fiche, puis classements, puis landings et carte. | La landing doit montrer de vraies fiches. |
| Toucher une ville sur la carte | Ouvre les classements de son pays. | La ville est saisie à la main et n'entre dans aucun classement. |
| E-mails gratuits sur la liste d'attente | Acceptés ; seule la forme de l'adresse est vérifiée. | Beaucoup de petites structures n'ont pas de domaine propre. |

## Règles non négociables

- Rien n'est publié sans l'accord du membre ; il peut dépublier à tout moment, effet immédiat.
- Chaque chiffre renvoie à sa preuve sur GitHub (lien vers la page GitHub qui le montre).
- La fiche dit clairement qu'elle mesure la contribution open source **publique**, pas la valeur
  totale d'une personne. Un excellent ingénieur qui travaille sur du code privé peut y paraître
  discret.
- Aucune preuve inventée sur le site : pas de faux témoignages, logos, chiffres ni profils. Une
  donnée d'illustration porte le libellé visible « Exemple ».
- Les informations saisies à la main (bio, LinkedIn, ville) sont affichées mais n'entrent
  jamais dans un classement.

## Parcours

1. **Connexion GitHub** (scope `read:user`).
2. **Analyse** automatique en tâche de fond (quelques secondes) : écran d'attente avec étapes.
3. **Aperçu privé** de la fiche + complétion : pays (obligatoire), ville, bio (importée de
   GitHub si elle existe, 160 caractères max), lien LinkedIn (identifiant seul).
4. **Publier** (ou rester privé). La fiche publique a une adresse stable, partageable.

Un calendrier vide n'est pas un échec : la fiche existe dès la première contribution et
affiche un chemin « Trouver un premier projet » plutôt qu'un zéro humiliant.

## Les six indicateurs

Tous calculés sur les données publiques GitHub, fenêtre glissante de 12 mois sauf mention.

| Indicateur | Définition | Source GitHub | Preuve (lien) |
| --- | --- | --- | --- |
| Ancienneté | Date de création du compte | `user.createdAt` | `github.com/{login}` |
| Volume | Contributions publiques sur 12 mois (commits, PR, issues, revues) ; les contributions privées comptées par GitHub sont exclues | `contributionsCollection.contributionCalendar.totalContributions` moins `restrictedContributionsCount` | `github.com/{login}` (calendrier) |
| Régularité | Nombre de mois actifs sur les 12 derniers, mois en cours compris | Jours du calendrier, regroupés par mois (GitHub y compte, jour par jour, les contributions privées d'un membre qui a choisi de les afficher ; on lit le calendrier tel qu'il le dessine) | `github.com/{login}` |
| Étoiles et followers | Somme des étoiles des dépôts publics possédés (forks exclus) ; nombre de followers | `repositories(ownerAffiliations: OWNER, isFork: false).stargazerCount`, `followers.totalCount` | `github.com/{login}?tab=repositories&sort=stargazers` |
| Langages | Répartition en octets sur les dépôts publics possédés, forks exclus ; 4 premiers + « Autres » | `repositories.languages(edges.size)` | `github.com/{login}?tab=repositories` |
| Contributions acceptées ailleurs | PR fusionnées dans des dépôts dont le membre n'est pas propriétaire, regroupées par dépôt | Recherche `is:pr is:merged author:{login} -user:{login}` | `github.com/pulls?q=is:pr+is:merged+author:{login}+-user:{login}` |

Chaque indicateur est stocké avec la date de l'analyse ; la fiche affiche « Données GitHub
publiques, analysées le … ». Rafraîchissement : à chaque connexion (au plus une fois par
heure) et chaque semaine par un job, dans les limites de débit de l'API.

## Écrans (maquettes « SourceFolio V1 », nov. 2026)

| Écran | Contenu |
| --- | --- |
| Landing | Sélecteur « Je suis un profil tech / Je recrute », carte de l'Afrique en mosaïque, champ `github.com/ton-pseudo` → « Voir ma fiche », sections Le problème, La fiche, Le chemin, Recruteurs, La méthode (ce qu'on lit / ce qu'on ne lit pas) |
| Landing recruteur | Promesse « Ne lisez plus de CV », champ de talents filtrable (langage, ville, rythme), inscription liste d'attente |
| Classements | Tri par critère (Régularité, Contributions, Étoiles, PR externes, Ancienneté), filtres pays et langage, tableau sur desktop, cartes sur mobile |
| Carte | Mosaïque de l'Afrique : chaque carré, la densité de profils publiés ; toucher une ville ouvre les classements de son pays |
| Fiche | Identité, bio, ville, liens ; les six indicateurs en cartes, calendrier de contributions, langages, contributions ailleurs, dépôts les plus suivis ; chaque chiffre cliquable vers GitHub |
| Analyse et complétion | Étapes Connexion → Analyse → Compléter → Publier, aperçu de la fiche à côté du formulaire |

Les positions (« #1 en régularité · Cameroun ») ne s'affichent que dans le top 10 d'un pays
ou du continent, pour ne pas transformer chaque fiche en rang.

## Design

- Niveau visuel de github.com : pages d’accueil sur fond nuit, application en clair, sombre ou
  système. Tokens dans `frontend/app/globals.css`.
- Geist pour toute l’interface, typographie très grande. Animations GSAP chargées après le
  premier affichage.
- Concept **« le carré vert »** : le carré de contribution est l'unité de toute l'interface :
  logo (2×2 carrés), carte de l'Afrique en mosaïque, chemin du débutant, champ de talents.
- Bilingue FR/EN, mobile d'abord (≈ 90 % des premières visites viennent d'un téléphone),
  cibles tactiles 44 px, contraste AA.

## Modèle et modules (backend)

| Module | Sort |
| --- | --- |
| `account` | Gardé (connexion GitHub, session, choix de compte) |
| `profile` | Réutilisé pour les champs saisis : pays, ville, bio, LinkedIn, visibilité |
| `contribution` (nouveau) | Passerelle GraphQL GitHub, analyse, instantanés des six indicateurs, job hebdomadaire |
| `discovery` | Réécrit : modèle de lecture des fiches publiées pour classements, filtres et carte |
| `waitlist` (nouveau) | Inscriptions recruteurs : e-mail pro, consentement, date |
| `showcase`, `checkup`, `engagement`, `storage` | Retirés, avec leurs tables et le stockage S3 |

## Hors V2

- Collecter des personnes non inscrites ; LinkedIn (aucune lecture, seulement le lien).
- Analyse des portfolios et des autres sources ; dépôts privés.
- Agents IA qui rédigent des fiches.
- Comptes recruteurs complets, messagerie, paiement, badges.

## La suite, dans l'ordre

1. Détection des outils, en commençant par les plus simples (Docker, CI).
2. Domaines déduits des outils (frontend, backend, DevOps, data, mobile).
3. Accès recruteur réel : recherche, puis contact.
4. Un éventuel score combiné, une fois qu'il y aura assez de vraies données pour l'ajuster.

## Livraison

| PR | Contenu |
| --- | --- |
| 1 | Cette spec, `CLAUDE.md` à jour |
| 2 | Module `contribution` : passerelle GraphQL, analyse, instantané, job, `GET /api/me/analysis` |
| 3 | Complétion et publication : pays, ville, bio, LinkedIn, visibilité ; `GET /api/profiles/{login}` (publiées seulement) |
| 4 | Front : tokens Primer, coquille (en-tête, thème, logo), étapes d'analyse, fiche |
| 5 | Classements et filtres, modèle de lecture `discovery` |
| 6 | Retrait des écrans, routes et jobs portfolio |
| 7 | Landings profil tech et recruteur, carte, liste d'attente |
| 8 | SEO et image de partage de la fiche |

## Encore à trancher

- Le nom (SourceFolio est provisoire).
- Les vraies preuves de la landing avant lancement : de vraies fiches, de vrais retours.

## Critères d'acceptation

- [ ] De la connexion GitHub à l'aperçu privé en moins de 30 secondes pour un compte moyen.
- [ ] Aucune fiche n'est visible publiquement sans action « Publier » du membre.
- [ ] Chaque chiffre de la fiche ouvre la page GitHub qui le prouve.
- [ ] La fiche affiche la date d'analyse et la phrase sur la contribution publique.
- [ ] Bio, LinkedIn et ville n'influencent aucun classement.
- [ ] Toutes les pages existent en français et en anglais, avec URL dédiée.
- [ ] LCP inférieur à 2,5 s en 4G sur un Android d'entrée de gamme.
- [ ] Aucune donnée d'illustration sans le libellé « Exemple ».
