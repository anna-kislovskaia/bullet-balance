aEmitentIds.map((id, i) => ({
  id,
  name: aEmitentNames[i],
  code: aEmitentCodes[i],
  market: aEmitentMarkets[i],
  child: aEmitentChild[i]
}))
.filter(item => item.code && item.child===1)
.map(item => `${item.id};${item.name};${item.code};${item.market}`)
.join("\n");

Finam.IssuerProfile.Main.markets.map(item => `${item.value};${item.title}`).join('\n');