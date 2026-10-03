#!/usr/bin/env python3
"""Builds app/src/main/assets/content/hinduism_en.json from Sir Edwin Arnold's
"The Song Celestial" (1885), public domain, Project Gutenberg eBook #2388.

PROVENANCE NOTE: unlike the KJV builder, this text could not be pulled
programmatically (the sandbox cannot reach gutenberg.org). The passages below
were copied by hand from the Gutenberg HTML text (pg2388-images.html). Arnold's
translation is not verse-numbered, so each passage is cited by chapter.
Spot-check a few against https://www.gutenberg.org/ebooks/2388 before release.
"""
import json, os
OUT = os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'assets', 'content', 'hinduism_en.json')
CATS = {111: ('Wisdom of the Soul', 'book_gita'), 112: ('Duty & Action', 'book_gita'),
        113: ('Peace & Self-Mastery', 'book_gita'), 114: ('Devotion & Virtue', 'book_gita')}
ROMAN = {2:'II',3:'III',4:'IV',5:'V',6:'VI',7:'VII',9:'IX',10:'X',12:'XII',13:'XIII',16:'XVI',17:'XVII',18:'XVIII'}
V = []
def add(cat, ch, tags, text): V.append((cat, ch, tags, text.strip('\n')))

# ---- Wisdom of the Soul
add(111,2,'soul,eternal,death','''Never the spirit was born; the spirit shall cease to be never;
Never was time it was not; End and Beginning are dreams!
Birthless and deathless and changeless remaineth the spirit for ever;
Death hath not touched it at all, dead though the house of it seems!''')
add(111,2,'soul,renewal,death','''Nay, but as when one layeth
His worn-out robes away,
And taking new ones, sayeth,
"These will I wear to-day!"
So putteth by the spirit
Lightly its garb of flesh,
And passeth to inherit
A residence afresh.''')
add(111,2,'soul,eternal','''I say to thee weapons reach not the Life;
Flame burns it not, waters cannot o'erwhelm,
Nor dry winds wither it. Impenetrable,
Unentered, unassailed, unharmed, untouched,
Immortal, all-arriving, stable, sure,
Invisible, ineffable, by word
And thought uncompassed, ever all itself,
Thus is the Soul declared!''')
add(111,2,'soul,eternal','''Nor I, nor thou, nor any one of these,
Ever was not, nor ever will not be,
For ever and for ever afterwards.
All, that doth live, lives always!''')
add(111,7,'truth,seeking','''Of many thousand mortals, one, perchance,
Striveth for Truth; and of those few that strive--
Nay, and rise high--one only--here and there--
Knoweth Me, as I am, the very Truth.''')
add(111,10,'presence,source','''I am the Spirit seated deep in every creature's heart;
From Me they come; by Me they live; at My word they depart!''')
add(111,13,'light,wisdom','''The Light of Lights He is, in the heart of the Dark
Shining eternally. Wisdom He is
And Wisdom's way, and Guide of all the wise,
Planted in every heart.''')
add(111,18,'knowledge,unity','''There is "true" Knowledge. Learn thou it is this:
To see one changeless Life in all the Lives,
And in the Separate, One Inseparable.''')
add(111,4,'righteousness,hope','''When Righteousness
Declines, O Bharata! when Wickedness
Is strong, I rise, from age to age, and take
Visible shape, and move a man with men,
Succouring the good, thrusting the evil back,
And setting Virtue on her seat again.''')
add(111,4,'knowledge,purity','''There is no purifier like thereto
In all this world, and he who seeketh it
Shall find it--being grown perfect--in himself.''')
add(111,4,'hope,forgiveness','''Moreover, Son of Pandu! wert thou worst
Of all wrong-doers, this fair ship of Truth
Should bear thee safe and dry across the sea
Of thy transgressions.''')
# ---- Duty & Action
add(112,2,'action,motive','''Find full reward
Of doing right in right! Let right deeds be
Thy motive, not the fruit which comes from them.
And live in action! Labour! Make thine acts
Thy piety, casting all self aside,
Contemning gain and merit; equable
In good or evil: equability
Is Yog, is piety!''')
add(112,2,'mind,inner-life','''Yet, the right act
Is less, far less, than the right-thinking mind.
Seek refuge in thy soul; have there thy heaven!''')
add(112,3,'work,duty','''Do thine allotted task!
Work is more excellent than idleness;
The body's life proceeds not, lacking work.''')
add(112,3,'duty,courage','''Finally, this is better, that one do
His own task as he may, even though he fail,
Than take tasks not his own, though they seem good.
To die performing duty is no ill;
But who seeks other roads shall wander still.''')
add(112,3,'example,leadership','''What the wise choose
The unwise people take; what best men do
The multitude will follow.''')
add(112,3,'purpose,service','''Yea! let each play his part
In all he finds to do, with unyoked soul.''')
add(112,18,'duty,contentment','''Better thine own work is, though done with fault,
Than doing others' work, ev'n excellently.''')
add(112,5,'detachment,calm','''He that acts in thought of Brahm,
Detaching end from act, with act content,
The world of sense can no more stain his soul
Than waters mar th' enamelled lotus-leaf.''')
add(112,4,'giving,knowledge','''The sacrifice
Which Knowledge pays is better than great gifts
Offered by wealth, since gifts' worth--O my Prince!
Lies in the mind which gives, the will that serves:
And these are gained by reverence, by strong search,
By humble heed of those who see the Truth
And teach it.''')
add(112,17,'giving,generosity','''The gift lovingly given, when one shall say
"Now must I gladly give!" when he who takes
Can render nothing back; made in due place,
Due time, and to a meet recipient,
Is gift of Sattwan, fair and profitable.''')
# ---- Peace & Self-Mastery
add(113,2,'desire,mind','''If one
Ponders on objects of the sense, there springs
Attraction; from attraction grows desire,
Desire flames to fierce passion, passion breeds
Recklessness; then the memory--all betrayed--
Lets noble purpose go, and saps the mind,
Till purpose, mind, and man are all undone.''')
add(113,2,'peace,tranquillity','''But, if one deals with objects of the sense
Not loving and not hating, making them
Serve his free soul, which rests serenely lord,
Lo! such a man comes to tranquillity;
And out of that tranquillity shall rise
The end and healing of his earthly pains,
Since the will governed sets the soul at peace.''')
add(113,2,'calm,equanimity','''In sorrows not dejected, and in joys
Not overjoyed; dwelling outside the stress
Of passion, fear, and anger; fixed in calms
Of lofty contemplation;--such an one
Is Muni, is the Sage, the true Recluse!''')
add(113,2,'desire,calm','''And like the ocean, day by day receiving
Floods from all lands, which never overflows
Its boundary-line not leaping, and not leaving,
Fed by the rivers, but unswelled by those;--

So is the perfect one! to his soul's ocean
The world of sense pours streams of witchery;
They leave him as they find, without commotion,
Taking their tribute, but remaining sea.''')
add(113,3,'mind,strength','''Yea, the world is strong,
But what discerns it stronger, and the mind
Strongest; and high o'er all the ruling Soul.''')
add(113,5,'unity,joy','''The world is overcome--aye! even here!
By such as fix their faith on Unity.''')
add(113,5,'balance,joy,sorrow','''Be not over-glad
Attaining joy, and be not over-sad
Encountering grief, but, stayed on Brahma, still
Constant let each abide!''')
add(113,6,'self,growth','''Let each man raise
The Self by Soul, not trample down his Self,
Since Soul that is Self's friend may grow Self's foe.''')
add(113,6,'meditation,focus','''Steadfast a lamp burns sheltered from the wind;
Such is the likeness of the Yogi's mind
Shut from sense-storms and burning bright to Heaven.''')
add(113,6,'balance,moderation','''But for earthly needs
Religion is not his who too much fasts
Or too much feasts, nor his who sleeps away
An idle mind; nor his who wears to waste
His strength in vigils. Nay, Arjuna! call
That the true piety which most removes
Earth-aches and ills, where one is moderate
In eating and in resting, and in sport;
Measured in wish and act; sleeping betimes,
Waking betimes for duty.''')
add(113,6,'mind,habit,patience','''Hero long-armed! beyond denial, hard
Man's heart is to restrain, and wavering;
Yet may it grow restrained by habit, Prince!
By wont of self-command.''')
add(113,6,'perseverance,hope','''He is not lost, thou Son of Pritha! No!
Nor earth, nor heaven is forfeit, even for him,
Because no heart that holds one right desire
Treadeth the road of loss!''')
# ---- Devotion & Virtue
add(114,6,'presence,love','''And whoso thus
Discerneth Me in all, and all in Me,
I never let him go; nor looseneth he
Hold upon Me; but, dwell he where he may,
Whate'er his life, in Me he dwells and lives,
Because he knows and worships Me, Who dwell
In all which lives, and cleaves to Me in all.''')
add(114,9,'offering,devotion','''Whoso shall offer Me in faith and love
A leaf, a flower, a fruit, water poured forth,
That offering I accept, lovingly made
With pious will. Whate'er thou doest, Prince!
Eating or sacrificing, giving gifts,
Praying or fasting, let it all be done
For Me, as Mine.''')
add(114,9,'love,grace','''I am alike for all! I know not hate,
I know not favour! What is made is Mine!
But them that worship Me with love, I love;
They are in Me, and I in them!''')
add(114,9,'hope,grace','''If one of evil life turn in his thought
Straightly to Me, count him amidst the good;
He hath the high way chosen; he shall grow
Righteous ere long; he shall attain that peace
Which changes not.''')
add(114,9,'trust,hope','''Thou Prince of India!
Be certain none can perish, trusting Me!''')
add(114,12,'compassion,virtue','''Who hateth nought
Of all which lives, living himself benign,
Compassionate, from arrogance exempt,
Exempt from love of self, unchangeable
By good or ill; patient, contented, firm
In faith, mastering himself, true to his word,
Seeking Me, heart and soul; vowed unto Me,--
That man I love!''')
add(114,12,'peace,gentleness','''Who troubleth not his kind,
And is not troubled by them; clear of wrath,
Living too high for gladness, grief, or fear,
That man I love!''')
add(114,12,'peace,renunciation','''Near to renunciation--very near--
Dwelleth Eternal Peace!''')
add(114,13,'humility,virtue','''Humbleness, truthfulness, and harmlessness,
Patience and honour, reverence for the wise.''')
add(114,16,'virtue,character','''Fearlessness, singleness of soul, the will
Always to strive for wisdom; opened hand
And governed appetites; and piety,
And love of lonely study; humbleness,
Uprightness, heed to injure nought which lives,
Truthfulness, slowness unto wrath, a mind
That lightly letteth go what others prize;''')
add(114,16,'warning,anger,greed','''The Doors of Hell
Are threefold, whereby men to ruin pass,--
The door of Lust, the door of Wrath, the door
Of Avarice. Let a man shun those three!''')
add(114,17,'speech,kindness','''Words causing no man woe, words ever true,
Gentle and pleasing words, and those ye say
In murmured reading of a Sacred Writ,--
These make the true religiousness of Speech.''')
add(114,17,'mind,serenity','''Serenity of soul, benignity,
Sway of the silent Spirit, constant stress
To sanctify the Nature,--these things make
Good rite, and true religiousness of Mind.''')

# interleave categories round-robin so consecutive days vary
b = {k: [v for v in V if v[0] == k] for k in CATS}; order = []
while any(b.values()):
    for k in CATS:
        if b[k]: order.append(b[k].pop(0))
vs = [{'id': 1100 + i, 'categoryId': c, 'dayNumber': i, 'originalText': '', 'translatedText': t,
       'sourceReference': f'The Song Celestial, Ch. {ROMAN[ch]} (Arnold, 1885)', 'tags': tags}
      for i, (c, ch, tags, t) in enumerate(order, 1)]
counts = {k: sum(1 for v in vs if v['categoryId'] == k) for k in CATS}
doc = {'religion': 'Hinduism', 'language': 'en', 'version': 2,
       'source': 'The Song Celestial (Bhagavad-Gita), tr. Sir Edwin Arnold, 1885. Public domain; Project Gutenberg #2388.',
       'categories': [{'id': k, 'name': n, 'icon': ic, 'verseCount': counts[k]} for k, (n, ic) in CATS.items()],
       'verses': vs}
with open(OUT, 'w', encoding='utf-8') as f: json.dump(doc, f, ensure_ascii=False, indent=2)
print(len(vs), 'passages', counts)
