#!/usr/bin/env python3
"""Builds app/src/main/assets/content/buddhism_en.json from the Dhammapada,
translated by F. Max Müller (Sacred Books of the East vol. X, 1881), public
domain; text from Project Gutenberg eBook #2017 (introduction and notes
omitted there). Verses are cited by their Dhammapada verse number.

PROVENANCE NOTE: hand-copied from the Gutenberg HTML (the sandbox cannot reach
gutenberg.org), so spot-check a few against https://www.gutenberg.org/ebooks/2017
before release. Gutenberg's text drops diacritics (Nirvana, Mara).
"""
import json, os
OUT = os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'assets', 'content', 'buddhism_en.json')
CATS = {401: ('Mind & Thought', 'book_dhammapada'), 402: ('Love & Anger', 'book_dhammapada'),
        403: ('Wisdom & Right Living', 'book_dhammapada'), 404: ('Peace & Happiness', 'book_dhammapada')}
T = {
1: "All that we are is the result of what we have thought: it is founded on our thoughts, it is made up of our thoughts. If a man speaks or acts with an evil thought, pain follows him, as the wheel follows the foot of the ox that draws the carriage.",
2: "All that we are is the result of what we have thought: it is founded on our thoughts, it is made up of our thoughts. If a man speaks or acts with a pure thought, happiness follows him, like a shadow that never leaves him.",
5: "For hatred does not cease by hatred at any time: hatred ceases by love, this is an old rule.",
13: "As rain breaks through an ill-thatched house, passion will break through an unreflecting mind.",
14: "As rain does not break through a well-thatched house, passion will not break through a well-reflecting mind.",
21: "Earnestness is the path of immortality (Nirvana), thoughtlessness the path of death. Those who are in earnest do not die, those who are thoughtless are as if dead already.",
25: "By rousing himself, by earnestness, by restraint and control, the wise man may make for himself an island which no flood can overwhelm.",
29: "Earnest among the thoughtless, awake among the sleepers, the wise man advances like a racer, leaving behind the hack.",
33: "As a fletcher makes straight his arrow, a wise man makes straight his trembling and unsteady thought, which is difficult to guard, difficult to hold back.",
35: "It is good to tame the mind, which is difficult to hold in and flighty, rushing wherever it listeth; a tamed mind brings happiness.",
36: "Let the wise man guard his thoughts, for they are difficult to perceive, very artful, and they rush wherever they list: thoughts well guarded bring happiness.",
42: "Whatever a hater may do to a hater, or an enemy to an enemy, a wrongly-directed mind will do us greater mischief.",
43: "Not a mother, not a father will do so much, nor any other relative; a well-directed mind will do us greater service.",
49: "As the bee collects nectar and departs without injuring the flower, or its colour or scent, so let a sage dwell in his village.",
51: "Like a beautiful flower, full of colour, but without scent, are the fine but fruitless words of him who does not act accordingly.",
52: "But, like a beautiful flower, full of colour and full of scent, are the fine and fruitful words of him who acts accordingly.",
60: "Long is the night to him who is awake; long is a mile to him who is tired; long is life to the foolish who do not know the true law.",
63: "The fool who knows his foolishness, is wise at least so far. But a fool who thinks himself wise, he is called a fool indeed.",
78: "Do not have evil-doers for friends, do not have low people for friends: have virtuous people for friends, have for friends the best of men.",
80: "Well-makers lead the water (wherever they like); fletchers bend the arrow; carpenters bend a log of wood; wise people fashion themselves.",
81: "As a solid rock is not shaken by the wind, wise people falter not amidst blame and praise.",
82: "Wise people, after they have listened to the laws, become serene, like a deep, smooth, and still lake.",
96: "His thought is quiet, quiet are his word and deed, when he has obtained freedom by true knowledge, when he has thus become a quiet man.",
98: "In a hamlet or in a forest, in the deep water or on the dry land, wherever venerable persons (Arhanta) dwell, that place is delightful.",
100: "Even though a speech be a thousand (of words), but made up of senseless words, one word of sense is better, which if a man hears, he becomes quiet.",
103: "If one man conquer in battle a thousand times thousand men, and if another conquer himself, he is the greatest of conquerors.",
122: "Let no man think lightly of good, saying in his heart, It will not come nigh unto me. Even by the falling of water-drops a water-pot is filled; the wise man becomes full of good, even if he gather it little by little.",
133: "Do not speak harshly to anybody; those who are spoken to will answer thee in the same way. Angry speech is painful, blows for blows will touch thee.",
160: "Self is the lord of self, who else could be the lord? With self well subdued, a man finds a lord such as few can find.",
165: "By oneself the evil is done, by oneself one suffers; by oneself evil is left undone, by oneself one is purified. Purity and impurity belong to oneself, no one can purify another.",
183: "Not to commit any sin, to do good, and to purify one's mind, that is the teaching of (all) the Awakened.",
197: "Let us live happily then, not hating those who hate us! among men who hate us let us dwell free from hatred!",
200: "Let us live happily then, though we call nothing our own! We shall be like the bright gods, feeding on happiness!",
201: "Victory breeds hatred, for the conquered is unhappy. He who has given up both victory and defeat, he, the contented, is happy.",
202: "There is no fire like passion; there is no losing throw like hatred; there is no pain like this body; there is no happiness higher than rest.",
204: "Health is the greatest of gifts, contentedness the best riches; trust is the best of relationships, Nirvana the highest happiness.",
205: "He who has tasted the sweetness of solitude and tranquillity, is free from fear and free from sin, while he tastes the sweetness of drinking in the law.",
222: "He who holds back rising anger like a rolling chariot, him I call a real driver; other people are but holding the reins.",
223: "Let a man overcome anger by love, let him overcome evil by good; let him overcome the greedy by liberality, the liar by truth!",
224: "Speak the truth, do not yield to anger; give, if thou art asked for little; by these three steps thou wilt go near the gods.",
232: "Beware of the anger of the tongue, and control thy tongue! Leave the sins of the tongue, and practise virtue with thy tongue!",
233: "Beware of the anger of the mind, and control thy mind! Leave the sins of the mind, and practise virtue with thy mind!",
234: "The wise who control their body, who control their tongue, the wise who control their mind, are indeed well controlled.",
252: "The fault of others is easily perceived, but that of oneself is difficult to perceive; a man winnows his neighbour's faults like chaff, but his own fault he hides, as a cheat hides the bad die from the gambler.",
258: "A man is not learned because he talks much; he who is patient, free from hatred and fear, he is called learned.",
261: "He in whom there is truth, virtue, love, restraint, moderation, he who is free from impurity and is wise, he is called an elder.",
270: "A man is not an elect (Ariya) because he injures living creatures; because he has pity on all living creatures, therefore is a man called Ariya.",
276: "You yourself must make an effort. The Tathagatas (Buddhas) are only preachers. The thoughtful who enter the way are freed from the bondage of Mara.",
285: "Cut out the love of self, like an autumn lotus, with thy hand! Cherish the road of peace. Nirvana has been shown by Sugata (Buddha).",
290: "If by leaving a small pleasure one sees a great pleasure, let a wise man leave the small pleasure, and look to the great.",
314: "An evil deed is better left undone, for a man repents of it afterwards; a good deed is better done, for having done it, one does not repent.",
326: "This mind of mine went formerly wandering about as it liked, as it listed, as it pleased; but I shall now hold it in thoroughly, as the rider who holds the hook holds in the furious elephant.",
327: "Be not thoughtless, watch your thoughts! Draw yourself out of the evil way, like an elephant sunk in mud.",
354: "The gift of the law exceeds all gifts; the sweetness of the law exceeds all sweetness; the delight in the law exceeds all delights; the extinction of thirst overcomes all pain.",
372: "Without knowledge there is no meditation, without meditation there is no knowledge: he who has knowledge and meditation is near unto Nirvana.",
379: "Rouse thyself by thyself, examine thyself by thyself, thus self-protected and attentive wilt thou live happily, O Bhikshu!",
380: "For self is the lord of self, self is the refuge of self; therefore curb thyself as the merchant curbs a good horse.",
}
G = {401: [1,2,13,14,33,35,36,42,43,326,327],
     402: [5,133,197,201,222,223,224,232,233,234,270],
     403: [21,25,29,49,51,52,60,63,78,80,81,82,100,103,122,160,165,183,252,258,261,276],
     404: [96,98,200,202,204,205,285,290,314,354,372,379,380]}
assert sorted(T) == sorted(n for v in G.values() for n in v), 'every selected verse must have text and vice versa'
assert all('\n' not in t for t in T.values())
b = {k: list(v) for k, v in G.items()}; order = []
while any(b.values()):
    for k in G:
        if b[k]: order.append((k, b[k].pop(0)))
vs = [{'id': 4000 + i, 'categoryId': c, 'dayNumber': i, 'originalText': '', 'translatedText': T[n],
       'sourceReference': f'Dhammapada {n} (Müller, 1881)', 'tags': ''} for i, (c, n) in enumerate(order, 1)]
counts = {k: len(v) for k, v in G.items()}
doc = {'religion': 'Buddhism', 'language': 'en', 'version': 1,
       'source': 'The Dhammapada, tr. F. Max Müller, Sacred Books of the East vol. X (1881). Public domain; Project Gutenberg #2017.',
       'categories': [{'id': k, 'name': n, 'icon': ic, 'verseCount': counts[k]} for k, (n, ic) in CATS.items()],
       'verses': vs}
with open(OUT, 'w', encoding='utf-8') as f: json.dump(doc, f, ensure_ascii=False, indent=2)
print(len(vs), 'verses', counts)
