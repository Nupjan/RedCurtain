# Design and Style Constants - RedCurtain App

## Colors

| Color Type | Color Name | Hex Code | Variable Name |
|------------|------------|----------|---------------|
| Primary color (Light) | Dark Red | #B71C1C | Red40 |
| Secondary color (Light) | Dark Red-Grey | #8B0000 | RedGrey40 |
| Accent color (Light) | Dark Crimson | #DC143C | Crimson40 |
| Primary Red | Red | #D32F2F | RedPrimary |
| Secondary Red | Light Red | #E57373 | RedSecondary |
| Accent Red | Bright Red | #FF5252 | RedAccent |
| Surface color | Light Pink | #FFF5F5 | RedSurface |
| Background color | Very Light Pink | #FFF8F8 | RedBackground |
| Primary color (Dark) | Light Red | #FFB3B3 | Red80 |
| Secondary color (Dark) | Light Red-Grey | #E8A8A8 | RedGrey80 |
| Accent color (Dark) | Light Crimson | #FF9999 | Crimson80 |
| Background (Dark) | Very Dark Red | #1A0000 | background |
| Surface (Dark) | Dark Red | #2D0000 | surface |
| Text on Background (Dark) | Light Pink | #FFE5E5 | onBackground |
| White color | White | #FFFFFF | White |
| Dark color | Black | #000000 | Black |

## Images

| Image Type | File Name | Description |
|------------|-----------|-------------|
| Logo | `logo.png` | Main app logo displayed on sign-in and sign-up screens |
| Background Image | `s_l1600.webp` | Cinematic background image used on authentication screens with 60% opacity overlay |
| Seat Available | `seat_available.xml` | Vector drawable for available seat state |
| Seat Reserved | `seat_reserved.xml` | Vector drawable for reserved seat state |
| Seat Selected | `seat_selected.xml` | Vector drawable for selected seat state |
| Dot Available | `dot_available.xml` | Legend indicator for available seats |
| Dot Reserved | `dot_reserved.xml` | Legend indicator for reserved seats |
| Dot Selected | `dot_selected.xml` | Legend indicator for selected seats |
| Arrow Back Icon | `ic_arrow_back.xml` | Navigation back button icon |
| Calendar Icon | `ic_calendar.xml` | Calendar/date picker icon |
| Button Gradient | `button_background_gradient.xml` | Gradient background for primary action buttons |
| Curved Indicator | `curved_line_indicator.xml` | Screen indicator design element |

All used images are found in: `app/src/main/res/drawable/` and `app/src/main/res/mipmap-*/`

### Image Sizes
- Logo: 212dp × 207dp (scaled for different densities)
- Background images: High quality, responsive sizing  
- Seat icons: 24dp × 24dp for individual seats
- Launcher icons: Provided in multiple densities (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)

## Text Styling

| Style Name | Font Family | Font Size | Font Weight |
|------------|-------------|-----------|-------------|
| bodyLarge | Default | 16.0sp | normal |
| Form Labels | Default | 18sp | normal |
| Large Headers | Default | 24sp | normal |

Typography follows Material Design 3 Typography system with default font family. A full listing can be found in: `app/src/main/java/com/example/redcurtainapp/ui/theme/Type.kt`

## Text Strings

| Text Type | Content |
|-----------|---------|
| App name | RedCurtainApp |
| Welcome title | Welcome to Red Curtains! |
| Sign In button | Sign In |
| Register link | Don't have an account? Register |
| Sign Up button | Sign Up |
| Sign In link | Already have an account? Sign In |
| Email label | Email |
| Password label | Password |
| Name label | Name |
| Phone label | Phone |
| Remember me | Remember me |

More text strings added during construction. A full listing can be found in: `app/src/main/res/values/strings.xml`
