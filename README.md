# AI Assistant

## Goal

Build a multiplatform AI assistant: Compose Multiplatform clients talk to a Ktor backend that stores conversations, orchestrates an agent, and answers with RAG, tools, and an LLM provider.

## Architecture

See the system diagram in [docs/architecture.md](docs/architecture.md#architecture).

## Current status

- Compose Multiplatform app for Android, iOS, Desktop, and Web JS
- Home screen checks API health and can create/load conversations
- Ktor gateway with `GET /api/v1/health` and conversation endpoints
- PostgreSQL schema for `users`, `conversations`, `messages`
- Redis cache for conversations in docker-dev
- Agent contract in `server:agent`: `LanguageModel`, `LlmRequest`, `LlmResponse`, `Message`, `Role`

Credentials live in `.env`. Copy the template before running locally:

```bash
cp .env.example .env
docker compose -f docker-compose.dev.yml up -d
```

Not implemented yet: LLM provider, RAG, MCP tools, chat API.

## Roadmap

- Wire a real `LanguageModel` implementation
- Chat API and persistence of conversations/messages
- RAG: embeddings, vector search, prompt builder
- Tools / MCP for the agent
- Client chat UI as a feature module
