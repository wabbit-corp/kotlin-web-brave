# Changelog

## Unreleased

- Replace the placeholder Brave unknown-field serializer with raw `JsonElement` handling so responses containing `discussions`, `infobox`, `locations`, and related structured fields no longer fail at runtime.
- Decode `web.results` as the full sealed `Result` hierarchy instead of only `SearchResult`, so Brave responses containing `location_result` entries parse successfully.
- Add a regression test covering Brave responses with previously-unhandled unknown structured fields.
- Add an optional `onSearchTrace` callback to Brave config so tests and callers can capture query metadata, raw response bodies, and errors for observability.
- Replace the old ad hoc JVM test with a real fixture replay test and a gated live fuzz test backed by filtered noun/verb word lists that persist failing payloads under `build/brave-live-failures/`.
