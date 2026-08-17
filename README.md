# AI Assistant

## Goal

Build a multiplatform AI assistant: Compose Multiplatform clients talk to a Ktor backend that stores conversations, orchestrates an agent, and answers with RAG, tools, and an LLM provider.

## Architecture

See the system diagram in [docs/architecture.md](docs/architecture.md#architecture).

## Current status

- Compose Multiplatform app for Android, iOS, Desktop, and Web JS
- Home screen checks API health
- Ktor gateway with `GET /health`
- PostgreSQL schema for `users`, `conversations`, `messages`
- Agent contract in `server:agent`: `LanguageModel`, `LlmRequest`, `LlmResponse`, `Message`, `Role`

Not implemented yet: LLM provider, RAG, Redis, MCP tools, chat API.

## Roadmap

- Wire a real `LanguageModel` implementation
- Chat API and persistence of conversations/messages
- RAG: embeddings, vector search, prompt builder
- Redis for cache / sessions
- Tools / MCP for the agent
- Client chat UI as a feature module
