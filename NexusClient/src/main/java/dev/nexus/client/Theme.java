package dev.nexus.client;

public final class Theme {
    public final String name;
    public final int bg, panel, accent, text;

    Theme(String name, int bg, int panel, int accent, int text) {
        this.name = name; this.bg = bg; this.panel = panel; this.accent = accent; this.text = text;
    }

    public static final Theme[] ALL = {
        new Theme("Lunar Dark",       0xB0141820, 0xE01E2430, 0xFF4DA3FF, 0xFFFFFFFF),
        new Theme("Feather Minimal",  0xA0202020, 0xF0262626, 0xFFFFFFFF, 0xFFEEEEEE),
        new Theme("Cyber Neon",       0xB00A0014, 0xE0140028, 0xFF00FFD5, 0xFFE0FFFF),
        new Theme("Glassmorphism",    0x60FFFFFF, 0x50FFFFFF, 0xFF9FD8FF, 0xFFFFFFFF),
        new Theme("Amoled Black",     0xFF000000, 0xFF000000, 0xFFFFFFFF, 0xFFFFFFFF),
        new Theme("Pastel Pink",      0xB0FFC1D9, 0xE0FFD6E6, 0xFFFF6FA5, 0xFF4A1F33),
        new Theme("Matrix Green",     0xB0001A00, 0xE0002B00, 0xFF00FF41, 0xFF7CFF9B),
        new Theme("Sunset Orange",    0xB02A1208, 0xE0401C0C, 0xFFFF7A1A, 0xFFFFE2C2),
        new Theme("Ocean Blue",       0xB0062A44, 0xE0083A5E, 0xFF1ECBE1, 0xFFE0F7FF),
        new Theme("Gold Luxury",      0xB0141008, 0xE0201A0A, 0xFFFFC83D, 0xFFFFF1C2),
        new Theme("Retro Pixel",      0xC0202040, 0xF0303060, 0xFFFFD000, 0xFFFFFFFF),
        new Theme("Crimson Red",      0xB0200608, 0xE0380A10, 0xFFE02030, 0xFFFFE0E0),
        new Theme("Frost White",      0xB0E8F4FF, 0xF0F5FAFF, 0xFF4AA8E8, 0xFF14324A),
        new Theme("Purple Galaxy",    0xB01A0933, 0xE0291152, 0xFFB36BFF, 0xFFF0E0FF),
        new Theme("Transparent Clean",0x40000000, 0x50000000, 0xFFFFFFFF, 0xFFFFFFFF),
    };

    public static Theme cur() {
        return ALL[Math.max(0, Math.min(ALL.length - 1, Config.theme))];
    }
}
