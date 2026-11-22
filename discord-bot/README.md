# YA CLIENT ticket bot

Private tickets with a category menu (Staff application, Partnership, Support, Other), Claim and Close buttons,
transcripts saved in a log channel, and `/add` `/remove` for staff.

## Setup
1. https://discord.com/developers/applications -> New Application -> Bot -> copy the **token**.
   No privileged intents are needed except **Message Content** (only used for the transcript): enable it in the Bot tab.
2. Invite the bot with scopes `bot` + `applications.commands` and permissions: Manage Channels, View Channels,
   Send Messages, Read Message History, Attach Files, Embed Links, Manage Roles (or just Administrator).
3. In Discord enable Developer Mode, then copy: server ID, staff role ID, ticket category ID, log channel ID.
4. `cp .env.example .env` and fill it in.
5. `npm install` then `npm start` (Node 18+). It must stay running: use a VPS, a Raspberry Pi or a bot host
   (or `pm2 start index.js --name ya-tickets`).
6. In the channel where people open tickets, run `/panel`.
