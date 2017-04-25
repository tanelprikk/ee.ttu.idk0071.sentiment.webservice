package ee.ttu.idk0071.sentiment.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ee.ttu.idk0071.sentiment.model.DomainLookup;
import ee.ttu.idk0071.sentiment.model.Lookup;
import ee.ttu.idk0071.sentiment.model.LookupEntity;
import ee.ttu.idk0071.sentiment.repository.LookupEntityRepository;
import ee.ttu.idk0071.sentiment.service.objects.DomainLookupResult;

@Service
public class LookupEntityService {
	@Autowired
	private LookupEntityRepository lookupEntityRepository;

	public List<LookupEntity> getAll() {
		return lookupEntityRepository.findAll();
	}

	public LookupEntity findByName(String rawEntityName) {
		return lookupEntityRepository.findByName(normalizeEntityName(rawEntityName));
	}

	public LookupEntity findById(Long id) {
		return lookupEntityRepository.findOne(id);
	}

	/**
	 * @return a sequence of domain lookup results for the specified entity and domain id combination
	 */
	public List<DomainLookupResult> getResultsForDomain(LookupEntity lookupEntity, Integer domainId) {
		List<Lookup> entityLookups = lookupEntity.getLookups();
		List<DomainLookupResult> statsSnapshots = entityLookups.stream()
				.flatMap(lookup -> lookup.getDomainLookups().stream())
				.filter(domainLookup -> domainLookup.getDomainLookupState().getCode() == DomainLookup.STATE_CODE_COMPLETE)
				.filter(domainLookup -> domainLookup.getDomain().getCode() == domainId)
				.filter(domainLookup -> domainLookup.getTotalCount() > 0)
				.map(DomainLookupResult::forDomainLookup)
				.sorted()
				.collect(Collectors.toList());
		return statsSnapshots;
	}

	/**
	 * Given a raw entity name, this method will:<br/>
	 * <ol>
	 *  <li>compress extraneous whitespace characters</li>
	 *  <li>remove leading/trailing whitespace characters</li>
	 *  <li>switch the string to lower case</li>
	 * </ol>
	 * The resultant entity name is ready for insertion into the database.
	 */
	public static String normalizeEntityName(String entityName) {
		return entityName.toLowerCase().trim().replaceAll("\\s+", " ");
	}
}