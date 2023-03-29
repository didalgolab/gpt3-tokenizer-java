package com.didalgo.gpt3;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GPT3TokenizerTest {

    @ParameterizedTest
    @CsvSource({
            "gpt-4, 'Stop!', '[10903, 0]'",
            "gpt-4, 'Stop now.', '[10903, 1457, 13]'",
            "gpt-4, 'Stop what you''re doing.', '[10903, 1148, 499, 2351, 3815, 13]'",
            "gpt-4, 'Stop what you''re doing right now.', '[10903, 1148, 499, 2351, 3815, 1314, 1457, 13]'",
            "gpt-4, 'Stop what you''re doing right now and listen.', '[10903, 1148, 499, 2351, 3815, 1314, 1457, 323, 9020, 13]'",
            "gpt-4, 'Stop what you''re doing right now and listen carefully.', '[10903, 1148, 499, 2351, 3815, 1314, 1457, 323, 9020, 15884, 13]'",
            "gpt-4, 'Stop what you''re doing right now and listen carefully to me.', '[10903, 1148, 499, 2351, 3815, 1314, 1457, 323, 9020, 15884, 311, 757, 13]'",
            "gpt-4, 'Stop what you''re doing right now and listen carefully to me, please.', '[10903, 1148, 499, 2351, 3815, 1314, 1457, 323, 9020, 15884, 311, 757, 11, 4587, 13]'",
            "gpt-4, 'Stop what you''re doing right now and listen carefully to me, please, because this is important.', '[10903, 1148, 499, 2351, 3815, 1314, 1457, 323, 9020, 15884, 311, 757, 11, 4587, 11, 1606, 420, 374, 3062, 13]'",
            "gpt-4, 'Stop what you''re doing right now and listen carefully to me, please, because this is very important.', '[10903, 1148, 499, 2351, 3815, 1314, 1457, 323, 9020, 15884, 311, 757, 11, 4587, 11, 1606, 420, 374, 1633, 3062, 13]'",
            "gpt-4, 'Przestań!', '[3617, 89, 30279, 19699, 0]'",
            "gpt-4, 'Przerwij to.', '[3617, 7215, 87183, 311, 13]'",
            "gpt-4, 'Przerwij to, co robisz.', '[3617, 7215, 87183, 311, 11, 1080, 10773, 70828, 13]'",
            "gpt-4, 'Przerwij to, co teraz robisz.', '[3617, 7215, 87183, 311, 11, 1080, 2024, 1394, 10773, 70828, 13]'",
            "gpt-4, 'Przerwij to, co teraz robisz i posłuchaj.', '[3617, 7215, 87183, 311, 11, 1080, 2024, 1394, 10773, 70828, 602, 1153, 4697, 1412, 1662, 13]'",
            "gpt-4, 'Przerwij to, co teraz robisz i posłuchaj uważnie.', '[3617, 7215, 87183, 311, 11, 1080, 2024, 1394, 10773, 70828, 602, 1153, 4697, 1412, 1662, 577, 10196, 6077, 11044, 13]'",
            "gpt-4, 'Przerwij to, co teraz robisz i posłuchaj mnie uważnie.', '[3617, 7215, 87183, 311, 11, 1080, 2024, 1394, 10773, 70828, 602, 1153, 4697, 1412, 1662, 74173, 577, 10196, 6077, 11044, 13]'",
            "gpt-4, 'Przerwij to, co teraz robisz i posłuchaj mnie uważnie, proszę.', '[3617, 7215, 87183, 311, 11, 1080, 2024, 1394, 10773, 70828, 602, 1153, 4697, 1412, 1662, 74173, 577, 10196, 6077, 11044, 11, 8882, 60705, 13]'",
            "gpt-4, 'Przerwij to, co teraz robisz i posłuchaj mnie proszę uważnie, bo to ważne.', '[3617, 7215, 87183, 311, 11, 1080, 2024, 1394, 10773, 70828, 602, 1153, 4697, 1412, 1662, 74173, 8882, 60705, 577, 10196, 6077, 11044, 11, 712, 311, 10667, 6077, 818, 13]'",
            "gpt-4, 'Przerwij to, co teraz robisz i posłuchaj mnie proszę uważnie, bo to bardzo ważne.', '[3617, 7215, 87183, 311, 11, 1080, 2024, 1394, 10773, 70828, 602, 1153, 4697, 1412, 1662, 74173, 8882, 60705, 577, 10196, 6077, 11044, 11, 712, 311, 57958, 10667, 6077, 818, 13]'",
            "gpt-4, 'СТІЙ!', '[19871, 35095, 140, 228, 140, 247, 0]'",
            "gpt-4, 'Припини зараз.', '[17279, 31203, 8164, 19479, 1840, 44946, 89554, 13]'",
            "gpt-4, 'Припини те, що ти робиш.', '[17279, 31203, 8164, 19479, 1840, 11047, 1532, 11, 9015, 231, 1482, 11047, 1840, 18600, 14082, 1840, 12426, 13]'",
            "gpt-4, 'Припини те, що ти робиш зараз.', '[17279, 31203, 8164, 19479, 1840, 11047, 1532, 11, 9015, 231, 1482, 11047, 1840, 18600, 14082, 1840, 12426, 44946, 89554, 13]'",
            "gpt-4, 'Припини те, що ти зараз робиш, і послухай.', '[17279, 31203, 8164, 19479, 1840, 11047, 1532, 11, 9015, 231, 1482, 11047, 1840, 44946, 89554, 18600, 14082, 1840, 12426, 11, 84954, 61813, 3865, 10693, 19039, 13]'",
            "gpt-4, 'Припини те, що ти зараз робиш, і уважно слухай.', '[17279, 31203, 8164, 19479, 1840, 11047, 1532, 11, 9015, 231, 1482, 11047, 1840, 44946, 89554, 18600, 14082, 1840, 12426, 11, 84954, 14257, 5591, 38657, 13999, 35875, 3865, 10693, 19039, 13]'",
            "gpt-4, 'Припини те, що ти зараз робиш, і вислухай мене уважно.', '[17279, 31203, 8164, 19479, 1840, 11047, 1532, 11, 9015, 231, 1482, 11047, 1840, 44946, 89554, 18600, 14082, 1840, 12426, 11, 84954, 5927, 13810, 3114, 3865, 10693, 19039, 69844, 1532, 14257, 5591, 38657, 13999, 13]'",
            "gpt-4, 'Припини те, що ти зараз робиш, і вислухай мене уважно, будь ласка.', '[17279, 31203, 8164, 19479, 1840, 11047, 1532, 11, 9015, 231, 1482, 11047, 1840, 44946, 89554, 18600, 14082, 1840, 12426, 11, 84954, 5927, 13810, 3114, 3865, 10693, 19039, 69844, 1532, 14257, 5591, 38657, 13999, 11, 51570, 4929, 26539, 18437, 13433, 13]'",
            "gpt-4, 'Припини те, що ти зараз робиш, і вислухай мене уважно, будь ласка, тому що це важливо.', '[17279, 31203, 8164, 19479, 1840, 11047, 1532, 11, 9015, 231, 1482, 11047, 1840, 44946, 89554, 18600, 14082, 1840, 12426, 11, 84954, 5927, 13810, 3114, 3865, 10693, 19039, 69844, 1532, 14257, 5591, 38657, 13999, 11, 51570, 4929, 26539, 18437, 13433, 11, 11047, 72952, 9015, 231, 1482, 39233, 1532, 5927, 38657, 11320, 5591, 1482, 13]'",
            "gpt-4, 'Припини те, що ти зараз робиш, і слухай мене уважно, бо це дуже важливо.', '[17279, 31203, 8164, 19479, 1840, 11047, 1532, 11, 9015, 231, 1482, 11047, 1840, 44946, 89554, 18600, 14082, 1840, 12426, 11, 84954, 35875, 3865, 10693, 19039, 69844, 1532, 14257, 5591, 38657, 13999, 11, 14391, 1482, 39233, 1532, 7952, 56999, 1532, 5927, 38657, 11320, 5591, 1482, 13]'",
            "gpt-4, 'class MyClass { public static void main(String[] args) { System.out.println(\"Hello, world!\"); }}', '[1058, 84926, 314, 586, 1118, 742, 1925, 2292, 1318, 2897, 8, 314, 744, 2594, 2986, 446, 9906, 11, 1917, 86640, 3954]'",
            "gpt-3.5-turbo, 'Stop!', '[10903, 0]'",
            "gpt-3.5-turbo, 'Stop now.', '[10903, 1457, 13]'",
            "gpt-3.5-turbo, 'Stop what you''re doing.', '[10903, 1148, 499, 2351, 3815, 13]'",
            "gpt-3.5-turbo, 'Stop what you''re doing right now.', '[10903, 1148, 499, 2351, 3815, 1314, 1457, 13]'",
            "gpt-3.5-turbo, 'Stop what you''re doing right now and listen.', '[10903, 1148, 499, 2351, 3815, 1314, 1457, 323, 9020, 13]'",
            "gpt-3.5-turbo, 'Stop what you''re doing right now and listen carefully.', '[10903, 1148, 499, 2351, 3815, 1314, 1457, 323, 9020, 15884, 13]'",
            "gpt-3.5-turbo, 'Stop what you''re doing right now and listen carefully to me.', '[10903, 1148, 499, 2351, 3815, 1314, 1457, 323, 9020, 15884, 311, 757, 13]'",
            "gpt-3.5-turbo, 'Stop what you''re doing right now and listen carefully to me, please.', '[10903, 1148, 499, 2351, 3815, 1314, 1457, 323, 9020, 15884, 311, 757, 11, 4587, 13]'",
            "gpt-3.5-turbo, 'Stop what you''re doing right now and listen carefully to me, please, because this is important.', '[10903, 1148, 499, 2351, 3815, 1314, 1457, 323, 9020, 15884, 311, 757, 11, 4587, 11, 1606, 420, 374, 3062, 13]'",
            "gpt-3.5-turbo, 'Stop what you''re doing right now and listen carefully to me, please, because this is very important.', '[10903, 1148, 499, 2351, 3815, 1314, 1457, 323, 9020, 15884, 311, 757, 11, 4587, 11, 1606, 420, 374, 1633, 3062, 13]'",
            "gpt-3.5-turbo, 'Przestań!', '[3617, 89, 30279, 19699, 0]'",
            "gpt-3.5-turbo, 'Przerwij to.', '[3617, 7215, 87183, 311, 13]'",
            "gpt-3.5-turbo, 'Przerwij to, co robisz.', '[3617, 7215, 87183, 311, 11, 1080, 10773, 70828, 13]'",
            "gpt-3.5-turbo, 'Przerwij to, co teraz robisz.', '[3617, 7215, 87183, 311, 11, 1080, 2024, 1394, 10773, 70828, 13]'",
            "gpt-3.5-turbo, 'Przerwij to, co teraz robisz i posłuchaj.', '[3617, 7215, 87183, 311, 11, 1080, 2024, 1394, 10773, 70828, 602, 1153, 4697, 1412, 1662, 13]'",
            "gpt-3.5-turbo, 'Przerwij to, co teraz robisz i posłuchaj uważnie.', '[3617, 7215, 87183, 311, 11, 1080, 2024, 1394, 10773, 70828, 602, 1153, 4697, 1412, 1662, 577, 10196, 6077, 11044, 13]'",
            "gpt-3.5-turbo, 'Przerwij to, co teraz robisz i posłuchaj mnie uważnie.', '[3617, 7215, 87183, 311, 11, 1080, 2024, 1394, 10773, 70828, 602, 1153, 4697, 1412, 1662, 74173, 577, 10196, 6077, 11044, 13]'",
            "gpt-3.5-turbo, 'Przerwij to, co teraz robisz i posłuchaj mnie uważnie, proszę.', '[3617, 7215, 87183, 311, 11, 1080, 2024, 1394, 10773, 70828, 602, 1153, 4697, 1412, 1662, 74173, 577, 10196, 6077, 11044, 11, 8882, 60705, 13]'",
            "gpt-3.5-turbo, 'Przerwij to, co teraz robisz i posłuchaj mnie proszę uważnie, bo to ważne.', '[3617, 7215, 87183, 311, 11, 1080, 2024, 1394, 10773, 70828, 602, 1153, 4697, 1412, 1662, 74173, 8882, 60705, 577, 10196, 6077, 11044, 11, 712, 311, 10667, 6077, 818, 13]'",
            "gpt-3.5-turbo, 'Przerwij to, co teraz robisz i posłuchaj mnie proszę uważnie, bo to bardzo ważne.', '[3617, 7215, 87183, 311, 11, 1080, 2024, 1394, 10773, 70828, 602, 1153, 4697, 1412, 1662, 74173, 8882, 60705, 577, 10196, 6077, 11044, 11, 712, 311, 57958, 10667, 6077, 818, 13]'",
            "gpt-3.5-turbo, 'СТІЙ!', '[19871, 35095, 140, 228, 140, 247, 0]'",
            "gpt-3.5-turbo, 'Припини зараз.', '[17279, 31203, 8164, 19479, 1840, 44946, 89554, 13]'",
            "gpt-3.5-turbo, 'Припини те, що ти робиш.', '[17279, 31203, 8164, 19479, 1840, 11047, 1532, 11, 9015, 231, 1482, 11047, 1840, 18600, 14082, 1840, 12426, 13]'",
            "gpt-3.5-turbo, 'Припини те, що ти робиш зараз.', '[17279, 31203, 8164, 19479, 1840, 11047, 1532, 11, 9015, 231, 1482, 11047, 1840, 18600, 14082, 1840, 12426, 44946, 89554, 13]'",
            "gpt-3.5-turbo, 'Припини те, що ти зараз робиш, і послухай.', '[17279, 31203, 8164, 19479, 1840, 11047, 1532, 11, 9015, 231, 1482, 11047, 1840, 44946, 89554, 18600, 14082, 1840, 12426, 11, 84954, 61813, 3865, 10693, 19039, 13]'",
            "gpt-3.5-turbo, 'Припини те, що ти зараз робиш, і уважно слухай.', '[17279, 31203, 8164, 19479, 1840, 11047, 1532, 11, 9015, 231, 1482, 11047, 1840, 44946, 89554, 18600, 14082, 1840, 12426, 11, 84954, 14257, 5591, 38657, 13999, 35875, 3865, 10693, 19039, 13]'",
            "gpt-3.5-turbo, 'Припини те, що ти зараз робиш, і вислухай мене уважно.', '[17279, 31203, 8164, 19479, 1840, 11047, 1532, 11, 9015, 231, 1482, 11047, 1840, 44946, 89554, 18600, 14082, 1840, 12426, 11, 84954, 5927, 13810, 3114, 3865, 10693, 19039, 69844, 1532, 14257, 5591, 38657, 13999, 13]'",
            "gpt-3.5-turbo, 'Припини те, що ти зараз робиш, і вислухай мене уважно, будь ласка.', '[17279, 31203, 8164, 19479, 1840, 11047, 1532, 11, 9015, 231, 1482, 11047, 1840, 44946, 89554, 18600, 14082, 1840, 12426, 11, 84954, 5927, 13810, 3114, 3865, 10693, 19039, 69844, 1532, 14257, 5591, 38657, 13999, 11, 51570, 4929, 26539, 18437, 13433, 13]'",
            "gpt-3.5-turbo, 'Припини те, що ти зараз робиш, і вислухай мене уважно, будь ласка, тому що це важливо.', '[17279, 31203, 8164, 19479, 1840, 11047, 1532, 11, 9015, 231, 1482, 11047, 1840, 44946, 89554, 18600, 14082, 1840, 12426, 11, 84954, 5927, 13810, 3114, 3865, 10693, 19039, 69844, 1532, 14257, 5591, 38657, 13999, 11, 51570, 4929, 26539, 18437, 13433, 11, 11047, 72952, 9015, 231, 1482, 39233, 1532, 5927, 38657, 11320, 5591, 1482, 13]'",
            "gpt-3.5-turbo, 'Припини те, що ти зараз робиш, і слухай мене уважно, бо це дуже важливо.', '[17279, 31203, 8164, 19479, 1840, 11047, 1532, 11, 9015, 231, 1482, 11047, 1840, 44946, 89554, 18600, 14082, 1840, 12426, 11, 84954, 35875, 3865, 10693, 19039, 69844, 1532, 14257, 5591, 38657, 13999, 11, 14391, 1482, 39233, 1532, 7952, 56999, 1532, 5927, 38657, 11320, 5591, 1482, 13]'",
            "gpt-3.5-turbo, 'class MyClass { public static void main(String[] args) { System.out.println(\"Hello, world!\"); }}', '[1058, 84926, 314, 586, 1118, 742, 1925, 2292, 1318, 2897, 8, 314, 744, 2594, 2986, 446, 9906, 11, 1917, 86640, 3954]'",
            "gpt-3.5-turbo, 'I''m', '[40, 2846]'",
            "gpt-3.5-turbo, 'I''m in', '[40, 2846, 304]'",
            "gpt-3.5-turbo, 'I''M', '[40, 28703]'",
            "gpt-3.5-turbo, 'I''M IN', '[40, 28703, 2006]'",
            "gpt-3.5-turbo, 'I''VE', '[40, 6, 4592]'",
            "gpt-3.5-turbo, 'I''VE DONE', '[40, 6, 4592, 55785]'",
            "gpt-3.5-turbo, 'I''ll', '[40, 3358]'",
            "gpt-3.5-turbo, 'I''ll do', '[40, 3358, 656]'",
            "gpt-3.5-turbo, 'I''D', '[40, 28805]'",
            "gpt-3.5-turbo, 'I''D DO', '[40, 28805, 9503]'",
            "gpt-3.5-turbo, 'I''d', '[40, 4265]'",
            "gpt-3.5-turbo, 'I''d done', '[40, 4265, 2884]'",
            "gpt-3.5-turbo, 'I''M', '[40, 28703]'",
            "gpt-3.5-turbo, 'I''M DONE', '[40, 28703, 55785]'",
            "gpt-3.5-turbo, 'you''re', '[9514, 2351]'",
            "gpt-3.5-turbo, 'you''re done', '[9514, 2351, 2884]'",
            "gpt-3.5-turbo, 'You''Re', '[2675, 50527]'",
            "gpt-3.5-turbo, 'You''Re Done', '[2675, 50527, 28457]'",
            "gpt-3.5-turbo, 'YOU''LL', '[57489, 6, 4178]'",
            "gpt-3.5-turbo, 'YOU''LL DO', '[57489, 6, 4178, 9503]'",
            "gpt-3.5-turbo, 'she''s', '[32158, 596]'",
            "gpt-3.5-turbo, 'she''s done', '[32158, 596, 2884]'",
            "gpt-3.5-turbo, 'SHE''S', '[50, 1837, 13575]'",
            "gpt-3.5-turbo, 'SHE''S DONE', '[50, 1837, 13575, 55785]'",
            "gpt-3.5-turbo, 'can''t', '[4919, 956]'",
            "gpt-3.5-turbo, 'can''t do', '[4919, 956, 656]'",
            "gpt-3.5-turbo, 'Can''T', '[6854, 17773]'",
            "gpt-3.5-turbo, 'CAN''T DO', '[43055, 17773, 9503]'",
            "gpt-3.5-turbo, 'c#ode', '[66, 2, 536]'",
            "gpt-3.5-turbo, 'java_language', '[10248, 30121]'",
            "gpt-3.5-turbo, 'regex{test}', '[27485, 90, 1985, 92]'",
            "gpt-3.5-turbo, 'python$', '[12958, 3]'",
            "gpt-3.5-turbo, 'python$code', '[12958, 3, 1889]'",
            "gpt-3.5-turbo, '3.14159265358979323846264338327950288419716939937510582097494459230781640628620899862803482534211706798214808651328230664709384460955058223172535940812848111745028410270193852110555', '[18, 13, 9335, 20128, 21598, 22905, 24531, 13895, 20911, 22956, 19230, 17267, 17824, 25962, 4468, 11739, 18572, 12935, 6550, 18248, 26007, 25687, 20128, 14777, 23713, 17264, 17361, 12171, 19416, 23574, 22379, 22091, 17590, 8546, 27309, 25873, 10410, 26956, 21164, 16544, 12879, 22644, 25202, 24344, 21138, 13506, 23670, 12245, 23309, 19192, 18058, 4386, 21235, 8546, 10617, 17058, 4278, 19597, 25454, 20767, 6550, 2131]'",
            "gpt-3.5-turbo, '😊', '[76460, 232]'",
            "gpt-3.5-turbo, '😂😍', '[76460, 224, 76460, 235]'",
            "gpt-3.5-turbo, '🤔😘😉', '[9468, 97, 242, 76460, 246, 76460, 231]'",
            "gpt-3.5-turbo, '🤯😴😜😝', '[9468, 97, 107, 76460, 112, 76460, 250, 76460, 251]'",
            "gpt-3.5-turbo, '😷🙄😶🤑😒', '[76460, 115, 9468, 247, 226, 76460, 114, 9468, 97, 239, 76460, 240]'",
            "gpt-3.5-turbo, '🤢🥺🥴🥵🥶🤕', '[9468, 97, 95, 9468, 98, 118, 9468, 98, 112, 9468, 98, 113, 9468, 98, 114, 9468, 97, 243]'",
            "gpt-3.5-turbo, '😭🤬🤪😈👹😻😼', '[76460, 255, 9468, 97, 105, 9468, 97, 103, 76460, 230, 9468, 239, 117, 76460, 119, 76460, 120]'",
            "gpt-3.5-turbo, '🤖💩👻👽🤡👺👾🧟‍♀️', '[9468, 97, 244, 93273, 102, 9468, 239, 119, 9468, 239, 121, 9468, 97, 94, 9468, 239, 118, 9468, 239, 122, 9468, 100, 253, 378, 235, 32990, 31643]'",
            "gpt-3.5-turbo, '🙏🏽🤲🏽👐🏽💪🏽👍🏽👎🏽✌🏽🤘🏽🤞🏽', '[9468, 247, 237, 9468, 237, 121, 9468, 97, 110, 9468, 237, 121, 9468, 80010, 9468, 237, 121, 93273, 103, 9468, 237, 121, 9468, 239, 235, 9468, 237, 121, 9468, 239, 236, 9468, 237, 121, 38798, 234, 9468, 237, 121, 9468, 97, 246, 9468, 237, 121, 9468, 97, 252, 9468, 237, 121]'",
            "gpt-3.5-turbo, '🌞🌈☀️❄️☔️🌊🍁🍂🌺🌸', '[9468, 234, 252, 9468, 234, 230, 18107, 222, 31643, 49633, 226, 31643, 18107, 242, 31643, 9468, 234, 232, 9468, 235, 223, 9468, 235, 224, 9468, 234, 118, 9468, 234, 116]'"
    })
    void can_encode_or_decode_test_vectors_correctly(String model,
                                                     String text,
                                                     @ConvertWith(ListConverter.class) List<Integer> tokens) {
        var enc = new GPT3Tokenizer(Encoding.forModel(model));
        assertEquals(tokens, enc.encode(text));
        assertEquals(text, enc.decode(tokens));
    }
}