require('dotenv').config();
const {
  Client, GatewayIntentBits, Partials, ChannelType, PermissionFlagsBits, EmbedBuilder, ActionRowBuilder,
  StringSelectMenuBuilder, ButtonBuilder, ButtonStyle, AttachmentBuilder, SlashCommandBuilder, REST, Routes,
} = require('discord.js');

const { TOKEN, GUILD_ID, STAFF_ROLE_ID, CATEGORY_ID, LOG_CHANNEL_ID } = process.env;
if (!TOKEN || !GUILD_ID || !STAFF_ROLE_ID || !CATEGORY_ID) {
  console.error('Missing values in .env (TOKEN, GUILD_ID, STAFF_ROLE_ID, CATEGORY_ID).');
  process.exit(1);
}

const RED = 0xe10600, DARK = 0x16171b;
const TYPES = {
  staff: { label: 'Staff application', emoji: '🛡️', desc: 'Apply to join the YA CLIENT staff team', prefix: 'staff' },
  partner: { label: 'Partnership', emoji: '🤝', desc: 'Become a partner of YA CLIENT', prefix: 'partner' },
  support: { label: 'Support', emoji: '🛠️', desc: 'Help with the client or the launcher', prefix: 'support' },
  other: { label: 'Other', emoji: '💬', desc: 'Anything else', prefix: 'ticket' },
};
const QUESTIONS = {
  staff: ['Your Minecraft name', 'Your age and country', 'Why do you want to join the staff team?', 'Previous experience (moderation, development, design, ...)'],
  partner: ['Your Minecraft name', 'Your channel / community / brand (with links)', 'Audience size', 'What do you propose?'],
  support: ['Your Minecraft name', 'Client or launcher version', 'Describe the problem (add screenshots or logs)'],
  other: ['Tell us how we can help'],
};

const client = new Client({
  intents: [GatewayIntentBits.Guilds, GatewayIntentBits.GuildMessages, GatewayIntentBits.MessageContent],
  partials: [Partials.Channel],
});

const commands = [
  new SlashCommandBuilder().setName('panel').setDescription('Post the ticket panel in this channel')
    .setDefaultMemberPermissions(PermissionFlagsBits.Administrator).toJSON(),
  new SlashCommandBuilder().setName('add').setDescription('Add a member to this ticket')
    .addUserOption(o => o.setName('user').setDescription('Member').setRequired(true))
    .setDefaultMemberPermissions(PermissionFlagsBits.ManageMessages).toJSON(),
  new SlashCommandBuilder().setName('remove').setDescription('Remove a member from this ticket')
    .addUserOption(o => o.setName('user').setDescription('Member').setRequired(true))
    .setDefaultMemberPermissions(PermissionFlagsBits.ManageMessages).toJSON(),
];

const isTicket = ch => ch && ch.type === ChannelType.GuildText && /^ticket:\d+/.test(ch.topic || '');
const ownerOf = ch => ((ch.topic || '').match(/^ticket:(\d+)/) || [])[1];
const isStaff = m => m.roles.cache.has(STAFF_ROLE_ID) || m.permissions.has(PermissionFlagsBits.Administrator);

client.once('ready', async () => {
  console.log(`Logged in as ${client.user.tag} (bot id ${client.user.id})`);
  const guild = client.guilds.cache.get(GUILD_ID);
  if (!guild) {
    const list = [...client.guilds.cache.values()].map(g => `${g.name} (${g.id})`).join(', ') || 'none';
    console.error(`The bot is NOT in the server with GUILD_ID ${GUILD_ID}. Servers it is in: ${list}`);
    console.error(`Invite THIS bot: https://discord.com/oauth2/authorize?client_id=${client.user.id}&permissions=8&integration_type=0&scope=bot+applications.commands`);
    return;
  }
  try {
    const rest = new REST({ version: '10' }).setToken(TOKEN);
    await rest.put(Routes.applicationGuildCommands(client.user.id, GUILD_ID), { body: commands });
    console.log(`Commands registered in "${guild.name}". YA CLIENT ticket bot ready as ${client.user.tag}`);
  } catch (e) {
    console.error('Could not register commands:', e.message);
    console.error(`Re-invite with commands scope: https://discord.com/oauth2/authorize?client_id=${client.user.id}&permissions=8&integration_type=0&scope=bot+applications.commands`);
  }
});

client.on('interactionCreate', async i => {
  try {
    if (i.isChatInputCommand()) return await onCommand(i);
    if (i.isStringSelectMenu() && i.customId === 'ticket:type') return await openTicket(i);
    if (i.isButton()) return await onButton(i);
  } catch (e) {
    console.error(e);
    const msg = { content: 'Something went wrong. Please try again or contact an admin.', ephemeral: true };
    if (i.deferred || i.replied) i.followUp(msg).catch(() => {}); else i.reply(msg).catch(() => {});
  }
});

async function onCommand(i) {
  if (i.commandName === 'panel') {
    const embed = new EmbedBuilder().setColor(RED).setTitle('YA CLIENT · Support & Applications')
      .setDescription('Choose a category below to open a private ticket with the YA CLIENT staff team.\n\n' +
        Object.values(TYPES).map(t => `${t.emoji} **${t.label}**: ${t.desc}`).join('\n') +
        '\n\nPlease open only one ticket at a time.');
    const menu = new StringSelectMenuBuilder().setCustomId('ticket:type').setPlaceholder('Select a category…')
      .addOptions(Object.entries(TYPES).map(([k, t]) => ({ label: t.label, description: t.desc, value: k, emoji: t.emoji })));
    await i.channel.send({ embeds: [embed], components: [new ActionRowBuilder().addComponents(menu)] });
    return i.reply({ content: 'Panel posted.', ephemeral: true });
  }
  if (!isTicket(i.channel)) return i.reply({ content: 'This command only works inside a ticket.', ephemeral: true });
  if (!isStaff(i.member)) return i.reply({ content: 'Staff only.', ephemeral: true });
  const user = i.options.getUser('user');
  if (i.commandName === 'add') {
    await i.channel.permissionOverwrites.edit(user.id, { ViewChannel: true, SendMessages: true, ReadMessageHistory: true, AttachFiles: true });
    return i.reply(`${user} was added to the ticket.`);
  }
  if (user.id === ownerOf(i.channel)) return i.reply({ content: 'You cannot remove the ticket owner.', ephemeral: true });
  await i.channel.permissionOverwrites.delete(user.id);
  return i.reply(`${user} was removed from the ticket.`);
}

async function openTicket(i) {
  const key = i.values[0], type = TYPES[key];
  await i.deferReply({ ephemeral: true });
  const existing = i.guild.channels.cache.find(c => isTicket(c) && ownerOf(c) === i.user.id);
  if (existing) return i.editReply(`You already have an open ticket: ${existing}`);
  const name = `${type.prefix}-${i.user.username}`.toLowerCase().replace(/[^a-z0-9-]/g, '').slice(0, 90) || `${type.prefix}-${i.user.id}`;
  const channel = await i.guild.channels.create({
    name, type: ChannelType.GuildText, parent: CATEGORY_ID, topic: `ticket:${i.user.id}:${key}`,
    permissionOverwrites: [
      { id: i.guild.roles.everyone.id, deny: [PermissionFlagsBits.ViewChannel] },
      { id: i.user.id, allow: [PermissionFlagsBits.ViewChannel, PermissionFlagsBits.SendMessages, PermissionFlagsBits.ReadMessageHistory, PermissionFlagsBits.AttachFiles] },
      { id: STAFF_ROLE_ID, allow: [PermissionFlagsBits.ViewChannel, PermissionFlagsBits.SendMessages, PermissionFlagsBits.ReadMessageHistory, PermissionFlagsBits.AttachFiles, PermissionFlagsBits.ManageMessages] },
      { id: client.user.id, allow: [PermissionFlagsBits.ViewChannel, PermissionFlagsBits.SendMessages, PermissionFlagsBits.ManageChannels, PermissionFlagsBits.ReadMessageHistory, PermissionFlagsBits.AttachFiles] },
    ],
  });
  const embed = new EmbedBuilder().setColor(DARK).setAuthor({ name: 'YA CLIENT' })
    .setTitle(`${type.emoji} ${type.label}`)
    .setDescription(`Welcome ${i.user}! The staff team will be with you shortly.\n\nTo help us, please answer the following:\n` +
      QUESTIONS[key].map((q, n) => `**${n + 1}.** ${q}`).join('\n'))
    .setFooter({ text: 'YA CLIENT Staff Team' }).setTimestamp();
  const row = new ActionRowBuilder().addComponents(
    new ButtonBuilder().setCustomId('ticket:claim').setLabel('Claim').setEmoji('✋').setStyle(ButtonStyle.Secondary),
    new ButtonBuilder().setCustomId('ticket:close').setLabel('Close').setEmoji('🔒').setStyle(ButtonStyle.Danger),
  );
  await channel.send({ content: `${i.user} <@&${STAFF_ROLE_ID}>`, embeds: [embed], components: [row] });
  return i.editReply(`Your ticket is ready: ${channel}`);
}

async function onButton(i) {
  if (!i.customId.startsWith('ticket:') || !isTicket(i.channel)) return;
  const owner = ownerOf(i.channel);
  if (i.customId === 'ticket:claim') {
    if (!isStaff(i.member)) return i.reply({ content: 'Staff only.', ephemeral: true });
    return i.reply({ embeds: [new EmbedBuilder().setColor(RED).setDescription(`✋ ${i.user} claimed this ticket.`)] });
  }
  if (i.customId === 'ticket:close') {
    if (!isStaff(i.member) && i.user.id !== owner) return i.reply({ content: 'Only the staff or the ticket owner can close it.', ephemeral: true });
    const row = new ActionRowBuilder().addComponents(
      new ButtonBuilder().setCustomId('ticket:confirm').setLabel('Confirm close').setStyle(ButtonStyle.Danger),
      new ButtonBuilder().setCustomId('ticket:cancel').setLabel('Cancel').setStyle(ButtonStyle.Secondary),
    );
    return i.reply({ content: 'Close this ticket? A transcript will be saved.', components: [row] });
  }
  if (i.customId === 'ticket:cancel') return i.update({ content: 'Cancelled.', components: [] });
  if (i.customId === 'ticket:confirm') {
    if (!isStaff(i.member) && i.user.id !== owner) return i.reply({ content: 'Only the staff or the ticket owner can close it.', ephemeral: true });
    await i.update({ content: '🔒 Closing in 5 seconds…', components: [] });
    await saveTranscript(i.channel, i.user);
    setTimeout(() => i.channel.delete('Ticket closed').catch(() => {}), 5000);
  }
}

async function saveTranscript(channel, closer) {
  if (!LOG_CHANNEL_ID) return;
  const log = await channel.guild.channels.fetch(LOG_CHANNEL_ID).catch(() => null);
  if (!log) return;
  let all = [], before;
  for (let n = 0; n < 20; n++) {
    const batch = await channel.messages.fetch({ limit: 100, before }).catch(() => null);
    if (!batch || !batch.size) break;
    all = all.concat([...batch.values()]);
    before = batch.last().id;
  }
  all.reverse();
  const text = all.map(m => `[${m.createdAt.toISOString()}] ${m.author.tag}: ${m.content}${m.attachments.size ? ' ' + [...m.attachments.values()].map(a => a.url).join(' ') : ''}`).join('\n');
  const owner = ownerOf(channel);
  const embed = new EmbedBuilder().setColor(RED).setTitle('Ticket closed')
    .addFields({ name: 'Ticket', value: channel.name, inline: true }, { name: 'Owner', value: `<@${owner}>`, inline: true }, { name: 'Closed by', value: `${closer}`, inline: true })
    .setTimestamp();
  await log.send({ embeds: [embed], files: [new AttachmentBuilder(Buffer.from(text || 'Empty ticket', 'utf8'), { name: `${channel.name}.txt` })] });
}

client.login(TOKEN);
