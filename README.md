<h1 id="team-2">Team 2</h1>
<p><a href="https://edu.tinkoff.ru/my-activities/courses/stream/80378e3d-aa9d-47c1-8af1-d6115e989712">edu</a></p>
<p>Куратор - <a href="https://t.me/chmhc">Михаил Чуряков</a></p>
<p> Состав:</p>
<ul>
<li>Егоров Гордей</li>
<li>Белый Антон</li>
<li>Хрол Ариана</li>
</ul>

<h2>Как запустить проект</h2>
<ol>
  <li>Убедитесь, что установлен docker-compose последней версии (проверено на версии v2.17.3)</li>
  <li>Убедитесь, что существует папка docker-compose/ в корне проекта, в которой созданы файлы database.env и routedatabase.env с записанными внутри переменными POSTGRES\_USER, POSTGRES\_PASSWORD, POSTGRES\_DB (значения должны быть согласованы с конфигами сервисов)</li>
  <li>Убедитесь, что локально на портах для сервисов и для постгреса не запущено больше никаких программ (в том числе нужно отключить свой psql, если он уже запущен)</li>
  <li>Чтобы почистить кэш: docker-compose down --volumes</li>
  <li>Чтобы собрать проект (например, после изменения кода): docker-compose build</li>
  <li>Чтобы запустить сервисы: docker-compose up</li>
</ol>
