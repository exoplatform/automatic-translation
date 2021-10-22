export function getConfiguration() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/automatic-translation/configuration`, {
   headers: {
     'Content-Type': 'application/json'
   },
   method: 'GET'
  }).then(resp => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Unable to get automatic translation configuration');
    }
  });
}

export function setActiveConnector(connector) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/automatic-translation/setActiveConnector?connector=${connector}`, {
   headers: {
     'Content-Type': 'application/json'
   },
   method: 'PUT'
  });
}

export function setApiKey(connector,apikey) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/automatic-translation/setApiKey?connector=${connector}&apikey=${apikey}`,
   {
   headers: {
     'Content-Type': 'application/json'
   },
   method: 'PUT'
  });
}

