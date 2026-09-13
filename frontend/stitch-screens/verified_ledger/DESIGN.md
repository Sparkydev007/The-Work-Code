---
name: Verified Ledger
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#434655'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#737686'
  outline-variant: '#c3c6d7'
  surface-tint: '#0053db'
  primary: '#004ac6'
  on-primary: '#ffffff'
  primary-container: '#2563eb'
  on-primary-container: '#eeefff'
  inverse-primary: '#b4c5ff'
  secondary: '#006a61'
  on-secondary: '#ffffff'
  secondary-container: '#86f2e4'
  on-secondary-container: '#006f66'
  tertiary: '#49566c'
  on-tertiary: '#ffffff'
  tertiary-container: '#616e85'
  on-tertiary-container: '#ebf1ff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dbe1ff'
  primary-fixed-dim: '#b4c5ff'
  on-primary-fixed: '#00174b'
  on-primary-fixed-variant: '#003ea8'
  secondary-fixed: '#89f5e7'
  secondary-fixed-dim: '#6bd8cb'
  on-secondary-fixed: '#00201d'
  on-secondary-fixed-variant: '#005049'
  tertiary-fixed: '#d6e3fe'
  tertiary-fixed-dim: '#bac7e1'
  on-tertiary-fixed: '#0e1c2f'
  on-tertiary-fixed-variant: '#3a475c'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 36px
    fontWeight: '600'
    lineHeight: 44px
    letterSpacing: -0.025em
  display-lg-mobile:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.015em
  headline-sm:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: -0.01em
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: -0.005em
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0em
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
    letterSpacing: 0.005em
  label-md:
    fontFamily: IBM Plex Sans
    fontSize: 13px
    fontWeight: '500'
    lineHeight: 18px
    letterSpacing: 0.01em
  label-sm:
    fontFamily: IBM Plex Sans
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.04em
  data-mono-md:
    fontFamily: IBM Plex Sans
    fontSize: 13px
    fontWeight: '500'
    lineHeight: 18px
    letterSpacing: -0.01em
  data-mono-sm:
    fontFamily: IBM Plex Sans
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0em
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-lg: 1.5rem
  margin: 1rem
  margin-md: 1.5rem
  margin-lg: 2rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-lg: 1rem
  space-xl: 1.5rem
---

## Brand & Style

This design system serves an enterprise-grade employment and income verification infrastructure. The brand personality conveys the precision of financial infrastructure, institutional durability, regulatory rigor, and absolute operational trust. It avoids decorative novelty in favor of utility, dense information design, and zero-latency readability needed by underwriters, risk analysts, compliance officers, and fintech developers.

The design movement is **Corporate / Modern** inflected with **High-Density Data Minimalism**. The aesthetic relies on structural integrity: hairline keylines (1px), subtle tonal framing, rigorous alignment grids, monospaced tabular figures for audit trails, and strict semantic signaling. Interfaces feel like a mission-critical financial terminal rebuilt with modern cloud ergonomics.

## Colors

The palette enforces clear semantic boundaries to support split-second underwriting decisions:

- **Primary Canvas & Surfaces**: Base application canvas rests on Ultra-Light Slate (`#F8FAFC`), while primary workspace surfaces and structured modules sit on Pure White (`#FFFFFF`). Secondary sub-panels and table headers leverage Soft Slate (`#F1F5F9`).
- **Core Neutrals & Text Hierarchy**: Highest-priority headings and data values use Deep Navy (`#0B192C`). Body copy, subheads, and secondary metadata use Cool Slate tones: Dark Slate (`#1E293B`), Mid Slate (`#334155`), Muted Slate (`#64748B`), and Hairline Borders (`#E2E8F0`).
- **Interactive Focus**: Cobalt Blue (`#2563EB`) governs primary triggers, active navigation tabs, selected states, and hyperlinked verification workflows. Hover states transition to Deep Cobalt (`#1D4ED8`).
- **Verification States (Semantic Feedback)**:
  - *Verified / Compliant*: Emerald/Teal (`#0D9488` text/border on `#F0FDF4` tint; `#10B981` indicators).
  - *Manual Review / Pending*: Amber (`#D97706` text/border on `#FFFBEB` tint; `#F59E0B` indicators).
  - *Risk / Discrepancy / Failed*: Crimson/Rose (`#DC2626` text/border on `#FEF2F2` tint; `#EF4444` indicators).

## Typography

Typography enforces tabular precision, high vertical cadence, and scan-friendly hierarchy. 

- **Primary Sans (Inter)** handles editorial and interactive tiers: headlines, modal headers, standard input controls, and primary narrative text. Tight negative tracking on display sizes enhances structural compactness.
- **Systematic Sans (IBM Plex Sans)** powers high-density components: field labels, metadata descriptions, audit badges, and data cells. 
- **Tabular Numerics**: For financial earnings, tax identification numbers, dates, and confidence percentages, enforce `font-feature-settings: "tnum" 1, "cv05" 1, "cv11" 1` across all table and key-value records to prevent layout jitter across dynamic updates.

## Layout & Spacing

The layout is built on a 12-column fluid grid system designed for desktop underwriting dashboards, scaling down gracefully for mobile field audits.

- **Desktop (1280px and above)**: 12 columns, 24px (`gutter-lg`) gutters, and 32px (`margin-lg`) canvas outer margins. Enables complex, multi-pane verification views, side-by-side document diffs, and sticky verification status rails.
- **Tablet / Small Laptop (768px - 1279px)**: 8 or 12 columns, 16px (`gutter`) gutters, 24px (`margin-md`) margins. Right-hand inspection drawers collapse into overlay panels.
- **Mobile (< 768px)**: 4 columns, 16px (`gutter`) gutters, 16px (`margin`) margins. Multi-column tables collapse into vertical card sequences.

The spacing scale is condensed (`space-md` is 12px / 0.75rem) to maintain density in data tables, compact key-value sidebars, and multi-field financial verification forms.

## Elevation & Depth

This design system avoids theatrical drop shadows and dramatic layered offsets. Depth is established through **low-contrast outlines** coupled with **crisp tonal layering**:

- **Border Hierarchy**: Visual segregation is maintained by a 1px border (`#E2E8F0`) surrounding white cards and containers over the `#F8FAFC` base surface. Inner dividers and row separators use soft `#F1F5F9`.
- **Z-Index Elevation**:
  - *Base Surface (0)*: Canvas at `#F8FAFC`.
  - *Resting Cards / Tables (Level 1)*: `#FFFFFF` background, 1px outline in `#E2E8F0`, zero shadow.
  - *Interactive / Floating Drawers (Level 2)*: Used for filter popovers, context menus, and verification breakdown sheets. Background `#FFFFFF`, 1px outline `#CBD5E1`, with a clinical shadow: `0px 4px 12px rgba(15, 23, 42, 0.06), 0px 1px 2px rgba(15, 23, 42, 0.04)`.
  - *Modals & Underwriter Dialogs (Level 3)*: Backed by a neutral deep navy scrim (`#0B192C` at 40% opacity), elevated with `0px 12px 32px rgba(15, 23, 42, 0.12), 0px 2px 4px rgba(15, 23, 42, 0.04)`.

## Shapes

The design system employs **Soft (1)** geometry to balance technical efficiency with modern UI standards:

- **Containers, Cards, and Tables**: Set to `0.25rem` (4px) to retain structural rigidity and maximize inner screen area.
- **Form Controls, Buttons, and Inputs**: Standardized at `0.25rem` (4px) radius.
- **Status Pills and Badges**: Exceptionally pill-shaped (`9999px`) to create an immediate morphological distinction between actionable UI controls (rectangular with 4px radius) and passive state badges (fully rounded).

## Components

### Buttons
- **Primary**: Solid Cobalt (`#2563EB`) background, white text, 4px border radius, 36px height (14px font, semi-bold). Active/focus states display a 2px offset focus ring (`#93C5FD`).
- **Secondary**: Pure White (`#FFFFFF`) surface, 1px border (`#E2E8F0`), Cool Slate (`#334155`) text. Hover introduces a subtle tint (`#F8FAFC`) and darker border (`#CBD5E1`).
- **Destructive / Flag Risk**: Pure White with Crimson outline (`#DC2626`) and text, transitioning to solid Crimson on critical confirmation.

### Badges & Verification Status Pills
- Compact height (20px to 22px), fully rounded (pill), 11px uppercase/small-caps type (`label-sm`).
- **Verified**: `#F0FDF4` background, `#166534` text, 1px `#BBF7D0` solid border, leading 6px emerald status dot.
- **Manual Review**: `#FFFBEB` background, `#92400E` text, 1px `#FDE68A` solid border, leading 6px amber status dot.
- **Risk / Discrepancy**: `#FEF2F2` background, `#991B1B` text, 1px `#FECACA` solid border, leading 6px crimson status dot.

### Data Tables
- Strict 40px cell height in compact mode; 48px in standard mode.
- Table headers set to 11px uppercase (`label-sm`) in `#64748B` with a `#F8FAFC` background and a solid bottom border (`#E2E8F0`).
- Alternating row zebra striping is rejected; data rows rest on `#FFFFFF` with hover transitions to `#F8FAFC`. Numbers are right-aligned and set with tabular figures.

### Form Inputs & Selectors
- 36px standard height, `#FFFFFF` background, 1px border (`#CBD5E1`).
- Placeholder text in `#94A3B8`. Focus state yields a crisp 1px Cobalt outline (`#2563EB`) and a 3px soft blue glow (`rgba(37, 99, 235, 0.15)`).
- Prefix and suffix micro-containers (e.g., currency symbols, SSN obfuscation toggles) feature `#F1F5F9` backgrounds and `#475569` icons.

### Key-Value Verification Rails
- Vertical summary arrays used within underwriter sidebars.
- Key labels rendered in `label-sm` Cool Slate (`#64748B`), values rendered in `data-mono-md` Deep Navy (`#0B192C`).
- Sections demarcated by 1px horizontal dividers (`#E2E8F0`) with 8px vertical padding per row.