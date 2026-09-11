# Application architecture

Each application feature uses the same inward dependency direction:

```text
presentation -> domain <- data
```

- `domain` owns entities, repository contracts, and business rules. It must not import Android UI or storage APIs.
- `data` implements domain contracts and owns Android, persistence, and runtime integration.
- `presentation` owns Compose UI, Android entry points, and view models.
- `core` is reserved for shared infrastructure with at least two feature consumers.
- A feature may depend on another feature only through that feature's domain surface.
- `packages/the_universe` contains the low-level `core`, `compiler`, and `reflection` modules. Keep their reflection and Android compatibility boundaries intact.
