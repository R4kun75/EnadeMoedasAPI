# Conversor de Moedas - Para Atividade Substituta Enade 

## Descrição do APP
Aplicativo Android nativo desenvolvido em Kotlin e Jetpack Compose para conversão de moedas em tempo real.

## Arquitetura
O projeto segue a arquitetura recomendada pela Google:
- Retrofit: Para consumo da API REST.
- ViewModel: Para gerenciamento de estado e lógica de negócios.
- Jetpack Compose: Para construção da UI declarativa.

- Professor, eu implementei a arquitetura com separação de responsabilidades (ViewModel, Repositório e UI).
- Como o app é 'Single Activity' e tem apenas uma funcionalidade, optei por manter as camadas no mesmo arquivo para facilitar a visualização do código completo durante o desenvolvimento, mas mantendo a separação lógica entre elas

## Capturas de Tela
<img width="493" height="984" alt="image" src="https://github.com/user-attachments/assets/7cd493b5-88b3-4ac9-9f11-5d30730d5c38" />

<img width="573" height="80" alt="image" src="https://github.com/user-attachments/assets/d6e68d7d-6397-4ee0-be05-74cf43e7575f" />

<img width="609" height="112" alt="image" src="https://github.com/user-attachments/assets/6571325d-5e2f-4555-a2e8-aff5d2f06920" />

## API Utilizada
- [Frankfurter API] (https://www.frankfurter.app/)

## Funcionalidades
- Listagem de moedas disponíveis.
- Conversão em tempo real.

## Referências
- [Guia de Arquitetura de Apps Android (Google)](https://developer.android.com/topic/architecture)
- [Documentação da API Frankfurter](https://www.frankfurter.app/docs/)
- [Vídeo Como consumir APIs com Retrofit usando Android e Kotlin](https://www.youtube.com/watch?v=U3Nmw0_BMVs).
 
