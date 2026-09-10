# Product and engineering decisions

- Android ships first with Kotlin and Jetpack Compose; phone and tablet share an adaptive workspace.
- The free tier receives 180 session-hours per monthly billing period and up to two concurrent sessions.
- Auto-click, macros, memory editing, fake GPS, root tooling, and anti-cheat bypass are out of scope.
- The initial UI validates session navigation and lifecycle; it does not pretend to run third-party games.
- Container-runtime feasibility is a gated spike before billing, backend, or broad game compatibility work.
- Visual design uses a low-overhead dark theme because the foreground game must retain the device budget.
