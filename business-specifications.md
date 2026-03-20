# ELO Ranking App — Business Specifications

## Context

The ELO Ranking App is a social platform that lets friends track and compare their skill levels across any competitive activity. Users create groups with their friends, define activities (ping-pong, chess, video games, or anything else), and record match results. The app calculates ELO ratings for each player based on match outcomes, providing fair and dynamic rankings that reflect everyone's performance over time. Whether it's a casual 1v1 or a multi-team tournament, the app supports any match format and keeps the competition fun and transparent.

---

## 1. User Accounts

- Users create an account with **email and password**
- Profile contains: **username** (unique across the app), **email**, **avatar**, **optional bio**
- Username can be changed (must remain unique)
- Users can **delete their account** — past matches are preserved, other players' ELO remain unchanged, the deleted user is anonymized
- Social login (Google, Apple…) may be added in the future
- App available on **web and mobile**

---

## 2. Groups

### Creation & Structure
- Any user can create a group
- **No maximum group size**
- A user can belong to **multiple groups**
- Everything within a group is **private to group members**

### Joining a Group
Three modes, configured at group level:
1. **Open** — anyone can join freely
2. **Invitation** — members/admins share an invite
3. **Request-to-join** — user requests, an admin approves or denies

### Administration
- The group **creator is admin by default**
- **Multiple admins** allowed
- Any admin can **promote** a member to admin
- An admin can only **demote themselves** — no one can force-remove an admin
- Admins can **remove members** from the group (no ban system)

### Archiving
- Admins can **archive and unarchive** a group
- While archived: members can **view history** but **nothing can be recorded**

---

## 3. Activities

### Creation & Structure
- Any **group member** can create an activity within a group
- Activities can be chosen from a **provided catalog** or created with **free text**
- A **starting ELO value** is set at activity creation and **cannot be changed afterwards**
- Members are **not automatically added** — they must be explicitly added

### Joining an Activity
Three modes, configured at activity level (same model as groups):
1. **Open**
2. **Invitation**
3. **Request-to-join** with admin approval

### Administration
- The activity **creator is admin by default**
- **Multiple admins** allowed
- Same promotion/demotion rules as groups (any admin promotes, admins can only demote themselves)
- Admins can **remove members** from the activity

### Archiving
- Admins can **archive and unarchive** an activity
- While archived: members can **view history** but **no new matches can be recorded**

---

## 4. Matches

### Creation
- Any **activity participant** can create a match
- The match creator **assigns all players to teams/sides**
- All participants must already be **members of the activity**
- **Minimum 2 players**, no maximum
- **Minimum 2 sides** (no cooperative mode)
- Any team composition is supported: 1v1, 2v2, 3v3, 1v1v1v1, 2v1, etc.

### Result Mode
Chosen at match creation:
1. **Full ranking** — all players/teams are ranked (1st, 2nd, 3rd…)
2. **Winner-only** — one winner, everyone else is equal

**Draws/ties are allowed** (shared rank or team tie).

### Result Recording
- Any **participant of the match** can record the result
- A match cannot be validated until a result is set

### Validation
Three modes, configured at the **activity level**:
1. **All participants** must validate
2. **Any single participant** can validate
3. **Only the match creator** can validate

If "all participants" mode: the match **stays pending** until everyone validates.

### ELO Update
- Once a match is **validated**, ELO is recalculated for all participants
- ELO change depends on the **current rating of the player relative to opponents and partners**

### Cancellation
- **Activity admins** can cancel a match at any stage
- **Pending match** cancelled → no ELO impact
- **Validated match** cancelled → ELO changes are **reverted** for all participants

---

## 5. Leaderboard & Statistics

### Leaderboard (per activity)
- Shows all participants ranked by **ELO**
- Displays **win/loss ratio** alongside ELO

### Player Detail (per player, per activity)
- **Match history** (results, opponents, dates)
- **ELO progression over time** (chart)
- **Win/loss streaks**
- **Head-to-head record** against other players

---

## 6. Out of Scope (future)
- Social login (Google, Apple…)
- Notifications (invites, match creation, validation requests, etc.)
- Ban system
